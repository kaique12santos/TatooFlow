package com.tattooflow.modules.convite;

import com.tattooflow.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/convites")
public class ConviteController {

    private final ConviteService conviteService;

    public ConviteController(ConviteService conviteService) {
        this.conviteService = conviteService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Convite>>> listarTodos() {
        return ResponseEntity.ok(ApiResponse.success(conviteService.listarTodos()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Convite>> criarConvite(@RequestBody Convite convite) {
        return ResponseEntity.ok(ApiResponse.success("Convite criado com sucesso", conviteService.criarConvite(convite)));
    }
}
