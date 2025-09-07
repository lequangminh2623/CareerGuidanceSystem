package com.lqm.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lqm.dtos.OrientationDTO;
import com.lqm.models.GradeDetail;
import com.lqm.models.Semester;
import com.lqm.models.User;
import com.lqm.dtos.SemesterAnalysisResult;
import com.lqm.services.GradeDetailService;
import com.lqm.services.SemesterService;
import com.lqm.services.UserService;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/secure/ai")
public class ApiAIController {

    @Autowired
    private GradeDetailService gradeDetailService;

    @Autowired
    private UserService userService;

    @Autowired
    private SemesterService semesterService;

    @GetMapping("/analysis/{semesterId}")
    public ResponseEntity<Map<String, Object>> clusterStudents(@PathVariable("semesterId") Integer semesterId) {
        // Nếu không có semesterId thì dùng mặc định là 1
        if (semesterId == null || semesterId <= 0) {
            semesterId = 1;
        }

        // Lấy email từ context đăng nhập
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = (principal instanceof String) ? (String) principal : null;

        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }

        User teacher = userService.getUserByEmail(email)
                .orElse(null);

        if (teacher == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Teacher not found"));
        }

        List<GradeDetail> gradeDetails = gradeDetailService.getGradeDetailsByTeacherAndSemester(teacher.getId(), semesterId);
        SemesterAnalysisResult result = gradeDetailService.analyzeSemester(gradeDetails);
        List<Semester> allSemesters = semesterService.getSemesters(null);

        Map<String, Object> response = new HashMap<>();
        response.put("analysisResult", result);
        response.put("semesters", allSemesters);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/ask")
    public String askAI(@RequestBody Map<String, String> payload) throws Exception {
        String userQuery = payload.get("query");
        if (userQuery == null || userQuery.isEmpty()) {
            throw new IllegalArgumentException("Query cannot be null or empty");
        }

        String apiUrl = "http://localhost:11434/api/chat";

        Map<String, Object> requestBody = Map.of(
                "model", "studentgrade-assistant:latest",
                "messages", List.of(
                        Map.of("role", "user", "content", userQuery)
                ),
                "stream", false
        );

        // Set up headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create the HTTP entity
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        // Initialize RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // Retry mechanism for handling empty responses
        String content = null;
        int maxRetries = 5; // Maximum number of retries
        int retryInterval = 1000; // Interval between retries in milliseconds

        for (int i = 0; i < maxRetries; i++) {
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, requestEntity, Map.class);

            // Extract the chatbot's reply
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("message")) {
                Map<String, Object> message = (Map<String, Object>) responseBody.get("message");
                content = (String) message.get("content");

                // Return the response if it's not empty
                if (content != null && !content.trim().isEmpty()) {
                    return content.trim();
                }
            }

            // Wait before retrying
            Thread.sleep(retryInterval);
        }

        // Fallback response if the content is empty after retries
        return "I'm sorry, I couldn't process your query. Please try again.";
    }

    @PostMapping(path = "/orientate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> orientate(
            @RequestBody com.lqm.dtos.OrientationDTO request
    ) {
        // 1) Lấy dữ liệu học thuật của người dùng hiện tại (gender + điểm các môn)
        User currentUser = this.userService.getCurrentUser();
        OrientationDTO dto = gradeDetailService.getSubjectAveragesForStudent(currentUser.getId());

        // 2) Ghép payload gửi sang service Python
        java.util.Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("gender", currentUser.isGender());
        payload.put("part_time_job", request.getPartTimeJob());
        payload.put("extracurricular_activities", request.getExtracurricularActivities());
        payload.put("absence_days", request.getAbsenceDays());
        payload.put("weekly_self_study_hours", request.getWeeklySelfStudyHours());
        payload.put("math_score", dto.getMathScore());
        payload.put("history_score", dto.getHistoryScore());
        payload.put("physics_score", dto.getPhysicsScore());
        payload.put("chemistry_score", dto.getChemistryScore());
        payload.put("biology_score", dto.getBiologyScore());
        payload.put("english_score", dto.getEnglishScore());
        payload.put("geography_score", dto.getGeographyScore());

        // 3) Gọi sang http://127.0.0.1:8000/orientate
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<java.util.Map<String, Object>> entity =
                new HttpEntity<>(payload, headers);

        final String url = "http://127.0.0.1:8000/orientate";
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<java.util.Map<String, Object>> pyResp =
                    restTemplate.exchange(
                            url, HttpMethod.POST, entity, new ParameterizedTypeReference<Map<String, Object>>() {}
                    );

            Map<String, Object> responseBody = pyResp.getBody();
            Map<String, Object> result = new LinkedHashMap<>();
            boolean ok = pyResp.getStatusCode().is2xxSuccessful();

            result.put("success", ok);
            // Lấy kết quả định hướng nghề nghiệp nếu có
            result.put("career_orientation", responseBody != null ? responseBody.get("career_orientation") : null);
            // Đính kèm toàn bộ phản hồi từ Python để frontend linh hoạt xử lý
            result.put("data", responseBody);

            return ResponseEntity.status(pyResp.getStatusCode()).body(result);
        } catch (RestClientResponseException ex) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("success", false);
            error.put("error", "Python service responded with an error");
            error.put("status", ex.getStatusCode());
            error.put("response", ex.getResponseBodyAsString());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
        } catch (Exception ex) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("success", false);
            error.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}