package com.tattooflow.modules.convite;

import com.tattooflow.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ConviteService {

    private final ConviteRepository conviteRepository;

    public ConviteService(ConviteRepository conviteRepository) {
        this.conviteRepository = conviteRepository;
    }

    public List<Convite> listarTodos() {
        return conviteRepository.findAll();
    }

    @Transactional
    public Convite buscarPorCodigo(String codigo) {
    return conviteRepository
            .buscarConviteValido(codigo, LocalDateTime.now())
            .orElseThrow(() -> new ResourceNotFoundException(
                    "Convite inválido, expirado ou já utilizado"));
}

    public Convite criarConvite(Convite convite) {
        Convite novoConvite = Convite.builder()
                .emailConvidado(convite.getEmailConvidado())
                .codigo(UUID.randomUUID().toString())
                .utilizado(false)
                .dataExpiracao(LocalDateTime.now().plusDays(7))
                .build();

        return conviteRepository.save(novoConvite);
    }
}
