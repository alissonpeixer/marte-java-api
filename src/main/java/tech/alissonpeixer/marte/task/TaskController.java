package tech.alissonpeixer.marte.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import tech.alissonpeixer.marte.utils.Utils;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private ITaskRepository taskRepository;

    @PostMapping("/")
    public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
        var idUser = request.getAttribute("idUser");
        taskModel.setIdUser((UUID) idUser);

        var currentDate = LocalDateTime.now();

        if (taskModel.getStartAt() == null || taskModel.getEndAt() == null) {
            return ResponseEntity.status(400).body("The start date and end date must be provided.");
        }

        if (currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt())) {
            return ResponseEntity.status(400).body("Start date and end date cannot be greater than today's date!");
        }

        if (taskModel.getStartAt().isAfter(taskModel.getEndAt())) {
            return ResponseEntity.status(400).body("Start date cannot be greater than end date");
        }

        var task = this.taskRepository.save(taskModel);

        return ResponseEntity.status(200).body(task);
    }

    @GetMapping("/")
    public List<TaskModel> list(HttpServletRequest request) {
        var tasks = this.taskRepository.findByIdUser((UUID) request.getAttribute("idUser"));
        return tasks;
    }

    @PutMapping("/{id}")
    public ResponseEntity update(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID id) {
        var task = this.taskRepository.findById(id).orElse(null);

        if (task == null) {
            return ResponseEntity.status(400).body("Tarefa não encontrada");
        }

        var idUser = request.getAttribute("idUser");

        if (idUser == null || (task.getIdUser() != null && !task.getIdUser().equals(idUser))) {
            return ResponseEntity.status(400)
                    .body("Usuário não tem permissão para alterar essa tarefa");
        }

        Utils.copyNonNullProperties(taskModel, task);
        var taskUpdated = this.taskRepository.save(task);
        return ResponseEntity.ok().body(taskUpdated);
    }

}
