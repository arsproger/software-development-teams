package com.developer.controllers;

import com.developer.dto.TaskDTO;
import com.developer.mapper.TaskMapper;
import com.developer.security.DeveloperDetails;
import com.developer.services.TaskService;
import com.itextpdf.text.DocumentException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/task")
public class TaskController {
    private final TaskService taskService;
    private final TaskMapper taskMapper;

    public TaskController(TaskService taskService, TaskMapper taskMapper) {
        this.taskService = taskService;
        this.taskMapper = taskMapper;
    }

    @GetMapping("/{id}")
    public TaskDTO getTaskById(@PathVariable Long id) {
        return taskMapper.convertToDTO(taskService.getTaskById(id));
    }

    @PostMapping
    public Long createTask(@RequestBody @Valid TaskDTO taskDTO,
                           BindingResult bindingResult,
                           @AuthenticationPrincipal DeveloperDetails developerDetails) {
        if (bindingResult.hasErrors())
            return null;

        return taskService.saveTask(developerDetails.getDeveloper().getId(),
                taskMapper.convertToEntity(taskDTO));
    }

    @PutMapping("/{id}")
    public Long updateTask(@PathVariable Long id, @RequestBody TaskDTO taskDTO) {
        return taskService.updateTask(id, taskMapper.convertToEntity(taskDTO));
    }

    @GetMapping("/report")
    public List<TaskDTO> createReport(@RequestParam String startDate,
                                      @RequestParam String endDate,
                                      @AuthenticationPrincipal DeveloperDetails developerDetails) {
        return taskService.createReportByDevId(developerDetails.getDeveloper().getId(),
                startDate, endDate).stream().map(
                taskMapper::convertToDTO).collect(Collectors.toList());
    }

    @GetMapping("/report/excel")
    public ResponseEntity<byte[]> exportToExcel(@RequestParam String startDate,
                                                @RequestParam String endDate,
                                                @AuthenticationPrincipal DeveloperDetails developerDetails) throws IOException {
        return taskService.exportToExcel(taskService.createReportByDevId(
                developerDetails.getDeveloper().getId(), startDate, endDate));
    }

    @GetMapping("/report/pdf")
    public ResponseEntity<byte[]> exportToPdf(@RequestParam String startDate,
                                                @RequestParam String endDate,
                                                @AuthenticationPrincipal DeveloperDetails developerDetails) throws IOException, DocumentException {
        return taskService.exportToPDF(taskService.createReportByDevId(
                developerDetails.getDeveloper().getId(), startDate, endDate));
    }

}
