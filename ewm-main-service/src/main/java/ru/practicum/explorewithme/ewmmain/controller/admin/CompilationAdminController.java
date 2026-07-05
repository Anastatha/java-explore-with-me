package ru.practicum.explorewithme.ewmmain.controller.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.ewmmain.dto.CompilationDto;
import ru.practicum.explorewithme.ewmmain.dto.NewCompilationDto;
import ru.practicum.explorewithme.ewmmain.dto.UpdateCompilationRequest;
import ru.practicum.explorewithme.ewmmain.service.CompilationService;

@RestController
@RequestMapping("/admin/compilations")
public class CompilationAdminController {
    private final CompilationService compilationService;

    public CompilationAdminController(CompilationService compilationService) {
        this.compilationService = compilationService;
    }

    @PostMapping
    public ResponseEntity<CompilationDto> saveCompilation(@Validated @RequestBody NewCompilationDto request) {
        return new ResponseEntity<>(compilationService.saveCompilation(request), HttpStatus.CREATED);
    }

    @DeleteMapping("/{compId}")
    public ResponseEntity<Void> deleteCompilation(@PathVariable Long compId) {
        compilationService.deleteCompilation(compId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{compId}")
    public CompilationDto updateCompilation(@PathVariable Long compId,
                                            @RequestBody UpdateCompilationRequest request) {
        return compilationService.updateCompilation(compId, request);
    }
}
