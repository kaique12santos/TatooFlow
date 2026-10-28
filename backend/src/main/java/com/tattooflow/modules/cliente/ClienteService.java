package com.tattooflow.modules.cliente;

import com.tattooflow.common.exception.ResourceNotFoundException;
import com.tattooflow.modules.termo.TermoAceite;
import com.tattooflow.modules.termo.TermoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private TermoRepository termoRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
        this.termoRepository = null;
    }

    @Autowired
    public ClienteService(ClienteRepository clienteRepository, TermoRepository termoRepository) {
        this.clienteRepository = clienteRepository;
        this.termoRepository = termoRepository;
    }

    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com ID: " + id));
    }

    public Cliente salvar(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    /**
     * Consulta os termos de consentimento vinculados ao histórico do cliente [F3-BACK-02].
     */
    public List<TermoAceite> buscarHistoricoTermos(Long clienteId) {
        buscarPorId(clienteId);
        if (termoRepository == null) {
            return List.of();
        }
        return termoRepository.findByClienteIdOrderByDataAssinaturaDesc(clienteId);
    }
}
