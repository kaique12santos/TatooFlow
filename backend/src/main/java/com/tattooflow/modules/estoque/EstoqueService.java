package com.tattooflow.modules.estoque;

import com.tattooflow.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EstoqueService {

    private final EstoqueRepository estoqueRepository;

    public EstoqueService(EstoqueRepository estoqueRepository) {
        this.estoqueRepository = estoqueRepository;
    }

    public List<ItemEstoque> listarTodos() {
        return estoqueRepository.findAll();
    }

    public ItemEstoque buscarPorId(Long id) {
        return estoqueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item de estoque não encontrado com ID: " + id));
    }

    public ItemEstoque salvar(ItemEstoque item) {
        return estoqueRepository.save(item);
    }
}
