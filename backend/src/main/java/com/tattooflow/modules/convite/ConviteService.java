package com.tattooflow.modules.convite;

import com.tattooflow.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConviteService {

    private final ConviteRepository conviteRepository;

    public ConviteService(ConviteRepository conviteRepository) {
        this.conviteRepository = conviteRepository;
    }

    public List<Convite> listarTodos() {
        return conviteRepository.findAll();
    }

    public Convite buscarPorCodigo(String codigo) {
        return conviteRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Convite inválido ou expirado: " + codigo));
    }

    public Convite criarConvite(Convite convite) {
        return conviteRepository.save(convite);
    }
}
