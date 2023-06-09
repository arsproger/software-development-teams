package com.developer.controllers;

import com.developer.dto.DeveloperDTOForAdmin;
import com.developer.dto.ReportDTO;
import com.developer.dto.TaskDTO;
import com.developer.dto.TeamDTO;
import com.developer.mapper.DeveloperDTOForAdminMapper;
import com.developer.mapper.ReportMapper;
import com.developer.mapper.TaskMapper;
import com.developer.mapper.TeamMapper;
import com.developer.models.Report;
import com.developer.services.DeveloperService;
import com.developer.services.ReportService;
import com.developer.services.TaskService;
import com.developer.services.TeamService;
import com.itextpdf.text.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final DeveloperService developerService;
    private final DeveloperDTOForAdminMapper developerMapper;
    private final TeamService teamService;
    private final TeamMapper teamMapper;
    private final TaskService taskService;
    private final TaskMapper taskMapper;
    private final ReportService reportService;
    private final ReportMapper reportMapper;

    @Autowired
    public AdminController(DeveloperService developerService, DeveloperDTOForAdminMapper developerMapper,
                           TeamService teamService, TeamMapper teamMapper,
                           TaskService taskService, TaskMapper taskMapper, ReportService reportService, ReportMapper reportMapper) {
        this.developerService = developerService;
        this.developerMapper = developerMapper;
        this.teamService = teamService;
        this.teamMapper = teamMapper;
        this.taskService = taskService;
        this.taskMapper = taskMapper;
        this.reportService = reportService;
        this.reportMapper = reportMapper;
    }

    @GetMapping("/developer/all")
    public List<DeveloperDTOForAdmin> getAllDevelopers() {
        return developerService.getAllDevelopers().stream().map(
                developerMapper::convertToDTO).collect(Collectors.toList());
    }

    @GetMapping("/developer/{id}")
    public DeveloperDTOForAdmin getDeveloperById(@PathVariable Long id) {
        return developerMapper.convertToDTO(developerService.getDeveloperById(id));
    }

    @DeleteMapping("/developer/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Long deleteDeveloperById(@PathVariable Long id) {
        return developerService.deleteDeveloperById(id);
    }

    @GetMapping("/team/all")
    public List<TeamDTO> getAllTeams() {
        return teamService.getAllTeams().stream().map(
                teamMapper::convertToDTO).collect(Collectors.toList());
    }

    @DeleteMapping("/team/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Long deleteTeamById(@PathVariable Long id) {
        return teamService.deleteTeamById(id);
    }

    @PostMapping("/team/new")
    @ResponseStatus(HttpStatus.CREATED)
    public Long saveTeam(@RequestBody @Valid TeamDTO teamDTO) {
        return teamService.saveTeam(teamMapper.convertToEntity(teamDTO));
    }

    @PutMapping("/team/addDev/{devId}/{teamId}")
    public Boolean addDevForTeam(@PathVariable Long devId, @PathVariable Long teamId) {
        return teamService.addDev(devId, teamId);
    }

    @PutMapping("/team/deleteDev/{devId}/{teamId}")
    public Boolean deleteDevForTeam(@PathVariable Long devId, @PathVariable Long teamId) {
        return teamService.deleteDev(devId, teamId);
    }

    @GetMapping("/task/all")
    public List<TaskDTO> getAllTask() {
        return taskService.getAllTasks().stream().map(
                taskMapper::convertToDTO).collect(Collectors.toList());
    }

    @DeleteMapping("/task/{id}")
    public Long deleteTask(@PathVariable Long id) {
        return taskService.deleteTask(id);
    }

    @GetMapping("/report/all")
    public List<ReportDTO> getAllReport() {
        return reportService.getAllReport().stream()
                .map(reportMapper::convertToDTO).collect(Collectors.toList());
    }

    @DeleteMapping("/report/{id}")
    public Long deleteReport(@PathVariable Long id) {
        return reportService.delete(id);
    }

    @GetMapping("/report/view")
    public List<TaskDTO> createReport(@RequestBody ReportDTO reportDTO) {
        Report report = reportMapper.convertToEntity(reportDTO);
        return reportService.createReport(report).stream().map(
                taskMapper::convertToDTO).collect(Collectors.toList());
    }

    @GetMapping("/report/excel")
    public ResponseEntity<byte[]> exportToExcel(@RequestParam String startDate,
                                                @RequestParam String endDate) throws IOException {

        return reportService.exportToExcel(reportService.reportForExportAllDevs(startDate, endDate));
    }

    @GetMapping("/report/pdf")
    public ResponseEntity<byte[]> exportToPdf(@RequestParam String startDate,
                                              @RequestParam String endDate) throws DocumentException {

        return reportService.exportToPDF(reportService.reportForExportAllDevs(startDate, endDate));
    }

}
