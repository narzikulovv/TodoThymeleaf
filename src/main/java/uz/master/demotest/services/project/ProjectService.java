package uz.master.demotest.services.project;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uz.master.demotest.configs.security.UserDetails;
import uz.master.demotest.dto.project.ProjectCreateDto;
import uz.master.demotest.dto.project.ProjectDto;
import uz.master.demotest.dto.project.ProjectUpdateDto;
import uz.master.demotest.entity.auth.AuthUser;
import uz.master.demotest.entity.project.Project;
import uz.master.demotest.mappers.ProjectMapper;
import uz.master.demotest.repositories.ProjectRepository;
import uz.master.demotest.services.AbstractService;
import uz.master.demotest.services.GenericCrudService;
import uz.master.demotest.services.column.ColumnService;
import uz.master.demotest.services.file.FileStorageService;
import uz.master.demotest.validator.project.ProjectValidator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService extends AbstractService<ProjectRepository, ProjectMapper, ProjectValidator>
        implements GenericCrudService<Project, ProjectDto, ProjectCreateDto, ProjectUpdateDto, Long> {


    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private final ColumnService columnService;
    private final FileStorageService fileStorageService;

    protected ProjectService(ProjectRepository repository, ProjectMapper mapper, ProjectValidator validator,
                             ColumnService columnService, FileStorageService fileStorageService) {
        super(repository, mapper, validator);
        this.columnService = columnService;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public Long create(ProjectCreateDto createDto) {
        MultipartFile file = createDto.getTz();
        String tzPath = fileStorageService.store(file);
        Project project = mapper.fromCreateDto(createDto);
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        project.setOrgId(principal.getOrganization());
        project.setTeamLeaderId(principal.getId());
        project.setTz(tzPath);
        return repository.save(project).getId();
    }

    @Override
    public Void delete(Long id) {
        repository.delete(id);
        return null;
    }

    @Override
    public Void update(ProjectUpdateDto updateDto) {
        repository.update(updateDto);
        return null;
    }

    @Override
    public List<ProjectDto> getAll() {
        return repository.findAllByDeletedFalse().stream().map(project -> {
            ProjectDto dto=mapper.toDto(project);
            dto.setCreatedAt(project.getCreatedAt().format(FORMATTER));
        return dto; }).collect(Collectors.toList());
    }
    public List<ProjectDto> getAll(Long id ) {
        return repository.findAllByOrgIdAndDeletedFalse(id).stream().map(project -> {
            ProjectDto dto=mapper.toDto(project);
            dto.setCreatedAt(project.getCreatedAt().format(FORMATTER));
            return dto; }).collect(Collectors.toList());
    }
    @Override
    public ProjectDto get(Long id) {
        ProjectDto projectDto = mapper.toDto(repository.findByIdAndDeletedFalse(id));
        projectDto.setColumns(columnService.getAll(projectDto.getId()));
        return projectDto;
    }

    public ProjectUpdateDto getUpdateDto(Long id) {
        return mapper.toUpdateDto(get(id));
    }

    public Long getTeamLead(Long projectId) {
        return repository.getTeamLead(projectId);
    }

    public List<AuthUser> getMembers(Long id) {
        //todo : Axrullo aka tugirlab yozib quying, men uzimga kerak buganinucun qildm,project Id kirib kesa undagi memberlarni bersin
        return new ArrayList<>() {{
            add(AuthUser.builder()
                    .id(3L)
                    .username("Kimdram")
                    .password("123123")
                    .build()
            );
        }};
    }
}
