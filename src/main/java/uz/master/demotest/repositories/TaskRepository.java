package uz.master.demotest.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import uz.master.demotest.entity.task.Task;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Task findByIdAndDeletedFalse(Long id);

    List<Task> findAllByDeletedFalse();

    List<Task> findAllByColumnIdAndDeletedFalse(Long columnId);

    @Transactional
    @Modifying
    @Query(value = "Update Tasks t SET t.deleted = true WHERE t.id=:id")
    void delete(@Param("id") Long id);

    @Transactional
    @Modifying
    @Query(value = "Update Tasks t SET t.name =:name,t.description =:description," +
            "t.taskOrder =:taskOrder,t.priority =:priority,t.level =:level,t.columnId =:columnId WHERE t.id=:id")
    void update(@Param("id") Long id, @Param("name") String name,
                @Param("description") String description, @Param("taskOrder") int taskOrder,
                @Param("level") String level, @Param("priority") String priority,
                @Param("columnId") Long columnId);

    @Modifying
    @Query(value = "insert into task_member (user_id,task_id) VALUES (:memberId,:taskId)", nativeQuery = true)
    @Transactional
    void addMember(@Param("taskId") Long taskId, @Param("memberId") Long memberId);

    @Modifying
    @Query("delete from Task_Member b where b.userId=:memberId and b.taskId=:taskId")
    @Transactional
    void deleteMember(@Param("taskId") Long taskId, @Param("memberId") Long memberId);
}
