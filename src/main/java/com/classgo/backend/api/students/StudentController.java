package com.classgo.backend.api.students;

import com.classgo.backend.api.students.dto.StudentDtos.ChildResponse;
import com.classgo.backend.api.students.dto.StudentDtos.JoinStudentRequest;
import com.classgo.backend.api.students.dto.StudentDtos.UpdateChildRequest;
import com.classgo.backend.application.students.StudentAffiliationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/students")
public class StudentController {

    private final StudentAffiliationService studentAffiliationService;

    public StudentController(StudentAffiliationService studentAffiliationService) {
        this.studentAffiliationService = studentAffiliationService;
    }

    @PostMapping("/join")
    public ChildResponse join(@Valid @RequestBody JoinStudentRequest request) {
        return studentAffiliationService.join(request);
    }

    @GetMapping("/my-children")
    public List<ChildResponse> myChildren() {
        return studentAffiliationService.myChildren();
    }

    @PutMapping("/my-children/{studentId}")
    public ChildResponse update(@PathVariable UUID studentId, @Valid @RequestBody UpdateChildRequest request) {
        return studentAffiliationService.update(studentId, request);
    }
}
