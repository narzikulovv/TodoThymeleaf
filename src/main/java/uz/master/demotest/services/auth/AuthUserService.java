package uz.master.demotest.services.auth;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import uz.master.demotest.configs.security.UserDetails;
import uz.master.demotest.dto.auth.*;

import uz.master.demotest.entity.auth.AuthRole;
import uz.master.demotest.entity.auth.AuthUser;
import uz.master.demotest.entity.auth.Token;
import uz.master.demotest.exceptions.NotFoundException;
import uz.master.demotest.mappers.AuthUserMapper;
import uz.master.demotest.repositories.AuthRoleRepository;
import uz.master.demotest.repositories.AuthUserRepository;
import uz.master.demotest.repositories.TokenRepository;
import uz.master.demotest.services.AbstractService;
import uz.master.demotest.services.GenericCrudService;
import uz.master.demotest.services.GenericService;
import uz.master.demotest.validator.auth.AuthUserValidator;
import uz.master.demotest.utils.SendEmail;

import java.util.*;

@Service
public class AuthUserService
        extends AbstractService<
        AuthUserRepository,
        AuthUserMapper,
        AuthUserValidator>
        implements GenericService<AuthDto,Long>,
        GenericCrudService<AuthUser,AuthDto, AuthUserCreateDto, AuthUserUpdateDto,Long> {
    private final TokenRepository tokenRepository;
    private final AuthRoleRepository roleRepository;
    private final  SendEmail email;
    private final PasswordEncoder encoder;
    protected AuthUserService(AuthUserRepository repository,
                              AuthUserMapper mapper,
                              AuthUserValidator validator,
                              TokenRepository tokenRepository, AuthRoleRepository roleRepository, SendEmail email, PasswordEncoder encoder) {
        super(repository, mapper, validator);
        this.tokenRepository = tokenRepository;
        this.roleRepository = roleRepository;
        this.email = email;
        this.encoder = encoder;
    }

    @Override
    public Long create(AuthUserCreateDto createDto) {
        AuthUser authUser = mapper.fromCreateDto(createDto);
        AuthRole byId = roleRepository.getById(createDto.getUserRole());
        authUser.setOrganizationId(((UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getOrganization());
        authUser.setRole(byId);
        authUser.setPassword(encoder.encode(createDto.getPassword()));
        return repository.save(authUser).getId();
    }


    public Long createAdmin(AddAdminDto addAdminDto, Long id) {
        AuthUser authUser = mapper.fromDto(addAdminDto);
        AuthRole byId = roleRepository.getById(1L);
        authUser.setOrganizationId(id);
        authUser.setRole(byId);
        authUser.setActive(true);
        authUser.setPassword(encoder.encode(addAdminDto.getPassword()));
        return repository.save(authUser).getId();
    }

    @Override
    public Void delete(Long id) {
        repository.deleteById(id);
        return null;
    }
    public Void delete(String username,String password) {
        Optional<AuthUser> user = repository.getAuthUsersByUsernameAndDeletedFalse(username);
        if (user.isEmpty()||!encoder.matches(password,user.get().getPassword())) throw new NotFoundException("bad Credential");
        repository.deleteUser(user.get().getId(),user.get().getUsername()+""+System.currentTimeMillis());
        return null;
    }



    @Override
    public Void update(AuthUserUpdateDto updateDto) {
        return null;
    }




    @Override
    public List<AuthDto> getAll() {
        return null;
    }

    @Override
    public AuthDto get(Long id) {
        AuthDto dto=new AuthDto();
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        dto.setId(principal.getId());
        dto.setOrgId(principal.getOrganization());
        dto.setFirstName(principal.getFistName());
        dto.setLastName(principal.getLastName());
        dto.setUsername(principal.getUsername());
        dto.setRole(principal.getRole());
        return dto;
    }

    public AuthUser get(String username) {
        Optional<AuthUser> authUserByUsername = repository.getAuthUsersByUsernameAndDeletedFalse(username);
        if (authUserByUsername.isEmpty()){
            throw new NotFoundException("username not found");
        }
        return authUserByUsername.get();
    }

    public void sendMail(String emailUser,String username) throws NotFoundException {
        if(Objects.nonNull(emailUser) &&emailUser.equals(get(username).getEmail())){
            String tokenGenerate =UUID.randomUUID().toString().replace("-","");
            Token token=new Token();
            token.setPrivateToken(tokenGenerate);
            tokenRepository.save(token);
            email.sendEmail(emailUser,tokenGenerate);
        }

    }

    public boolean checkToken(String token) {
        return tokenRepository.findByPrivateToken(token).isEmpty();

    }

    // TODO: 2/26/2022 sh yerga yozib ketishim kerak validatorlarni
    public void resetPassword(ResetPassword dto){
        AuthUser authUser = get(dto.getUsername());
        authUser.setPassword(encoder.encode(dto.getPassword()));
        repository.save(authUser);
    }
}
