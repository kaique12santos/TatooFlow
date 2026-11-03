package com.tattooflow.modules.convite;

import com.tattooflow.common.exception.ResourceNotFoundException;
import com.tattooflow.modules.usuario.Usuario;
import com.tattooflow.modules.usuario.UsuarioService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ConviteService {

    private final ConviteRepository conviteRepository;
    private final UsuarioService usuarioService;

    public ConviteService(ConviteRepository conviteRepository, UsuarioService usuarioService) {
        this.conviteRepository = conviteRepository;
        this.usuarioService = usuarioService;
    }

    public List<Convite> listarTodos() {
        return conviteRepository.findAll();
    }

    public List<EquipeConviteResponse> listarConvitesEquipe() {
        return conviteRepository.findAll().stream()
                .map(EquipeConviteResponse::from)
                .toList();
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

    @Transactional
    public Convite reenviarConvite(Long id) {
        Convite convite = conviteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Convite não encontrado com ID: " + id));
        if (Boolean.TRUE.equals(convite.getUtilizado())) {
            throw new com.tattooflow.common.exception.BusinessException("Convite já utilizado não pode ser reenviado");
        }
        convite.setDataExpiracao(LocalDateTime.now().plusDays(7));
        return conviteRepository.save(convite);
    }

    @Transactional
    public Usuario aceitarConvite(String codigo, AceitarConviteRequest request) {
        Convite convite = buscarPorCodigo(codigo);

        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(convite.getEmailConvidado())
                .senha(request.pin())
                .aparelhoId(request.aparelhoId())
                .perfil("TATUADOR")
                .ativo(true)
                .build();

        Usuario salvo = usuarioService.salvar(usuario);

        convite.setUtilizado(true);
        conviteRepository.save(convite);

        return salvo;
    }
}
