package com.tattooflow.modules.estoque;

import com.tattooflow.common.exception.BusinessException;
import com.tattooflow.common.exception.ResourceNotFoundException;
import com.tattooflow.integrations.whatsapp.WhatsAppMessageSender;
import com.tattooflow.modules.usuario.Usuario;
import com.tattooflow.modules.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstoqueServiceTest {

    @Mock
    private EstoqueRepository estoqueRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private WhatsAppMessageSender whatsAppMessageSender;

    @InjectMocks
    private EstoqueService estoqueService;

    private Usuario adminMock;

    @BeforeEach
    void setUp() {
        estoqueService.limparHistoricoAlertas();
        adminMock = Usuario.builder()
                .id(1L)
                .nome("Admin Studio")
                .email("admin@tattooflow.com")
                .perfil("ADMIN")
                .ativo(true)
                .build();
    }

    // =========================================================================
    // [F7-BACK-01] TESTES DO CRUD DE ITENS DE ESTOQUE
    // =========================================================================

    @Test
    @DisplayName("[F7-BACK-01] Deve cadastrar item de estoque com sucesso quando quantidade não for negativa")
    void deveCriarItemEstoqueComSucesso() {
        ItemEstoque novo = ItemEstoque.builder()
                .nome("Agulha 05RL")
                .categoria("AGULHAS")
                .quantidade(50)
                .quantidadeMinima(10)
                .unidadeMedida("unidades")
                .build();

        when(estoqueRepository.save(any(ItemEstoque.class))).thenAnswer(i -> {
            ItemEstoque item = i.getArgument(0);
            item.setId(1L);
            return item;
        });

        ItemEstoque criado = estoqueService.criar(novo);

        assertThat(criado).isNotNull();
        assertThat(criado.getId()).isEqualTo(1L);
        assertThat(criado.getNome()).isEqualTo("Agulha 05RL");
        assertThat(criado.getQuantidade()).isEqualTo(50);
        assertThat(criado.getQuantidadeMinima()).isEqualTo(10);
        verify(estoqueRepository).save(any(ItemEstoque.class));
    }

    @Test
    @DisplayName("[F7-BACK-01] Deve rejeitar cadastro com quantidade negativa")
    void deveRejeitarCadastroComQuantidadeNegativa() {
        ItemEstoque item = ItemEstoque.builder()
                .nome("Tinta Preta")
                .categoria("TINTAS")
                .quantidade(-5)
                .quantidadeMinima(2)
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () -> estoqueService.criar(item));
        assertThat(ex.getMessage()).contains("A quantidade em estoque não pode ser negativa");
        verifyNoInteractions(estoqueRepository);
    }

    @Test
    @DisplayName("[F7-BACK-01] Deve rejeitar cadastro com quantidade mínima negativa")
    void deveRejeitarCadastroComQuantidadeMinimaNegativa() {
        ItemEstoque item = ItemEstoque.builder()
                .nome("Luva Látex P")
                .categoria("LUVAS")
                .quantidade(10)
                .quantidadeMinima(-1)
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () -> estoqueService.criar(item));
        assertThat(ex.getMessage()).contains("A quantidade mínima não pode ser negativa");
        verifyNoInteractions(estoqueRepository);
    }

    @Test
    @DisplayName("[F7-BACK-01] Deve rejeitar cadastro com nome vazio ou nulo")
    void deveRejeitarCadastroComNomeVazio() {
        ItemEstoque itemVazio = ItemEstoque.builder().nome("   ").quantidade(10).build();
        assertThrows(BusinessException.class, () -> estoqueService.criar(itemVazio));

        ItemEstoque itemNulo = ItemEstoque.builder().nome(null).quantidade(10).build();
        assertThrows(BusinessException.class, () -> estoqueService.criar(itemNulo));
        verifyNoInteractions(estoqueRepository);
    }

    @Test
    @DisplayName("[F7-BACK-01] Deve atualizar item de estoque com sucesso")
    void deveAtualizarItemEstoqueComSucesso() {
        ItemEstoque existente = ItemEstoque.builder()
                .id(10L)
                .nome("Tinta Dynamic Black")
                .categoria("TINTAS")
                .quantidade(8)
                .quantidadeMinima(3)
                .unidadeMedida("frascos")
                .build();

        ItemEstoque atualizacao = ItemEstoque.builder()
                .nome("Tinta Dynamic Black 240ml")
                .categoria("TINTAS")
                .quantidade(12)
                .quantidadeMinima(4)
                .unidadeMedida("frascos")
                .build();

        when(estoqueRepository.findById(10L)).thenReturn(Optional.of(existente));
        when(estoqueRepository.save(any(ItemEstoque.class))).thenAnswer(i -> i.getArgument(0));

        ItemEstoque atualizado = estoqueService.atualizar(10L, atualizacao);

        assertThat(atualizado.getNome()).isEqualTo("Tinta Dynamic Black 240ml");
        assertThat(atualizado.getQuantidade()).isEqualTo(12);
        assertThat(atualizado.getQuantidadeMinima()).isEqualTo(4);
    }

    @Test
    @DisplayName("[F7-BACK-01] Deve rejeitar atualização com quantidade negativa")
    void deveRejeitarAtualizacaoComQuantidadeNegativa() {
        ItemEstoque existente = ItemEstoque.builder().id(10L).nome("Item").quantidade(5).quantidadeMinima(2).build();
        ItemEstoque invalido = ItemEstoque.builder().nome("Item").quantidade(-2).build();

        when(estoqueRepository.findById(10L)).thenReturn(Optional.of(existente));

        assertThrows(BusinessException.class, () -> estoqueService.atualizar(10L, invalido));
    }

    @Test
    @DisplayName("[F7-BACK-01] Deve remover item de estoque existente")
    void deveDeletarItemEstoque() {
        ItemEstoque existente = ItemEstoque.builder().id(10L).nome("Item Removivel").build();
        when(estoqueRepository.findById(10L)).thenReturn(Optional.of(existente));

        estoqueService.deletar(10L);

        verify(estoqueRepository).delete(existente);
    }

    @Test
    @DisplayName("[F7-BACK-01] Deve lançar ResourceNotFoundException ao tentar remover item inexistente")
    void deveLancarExcecaoAoDeletarItemInexistente() {
        when(estoqueRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> estoqueService.deletar(999L));
        verify(estoqueRepository, never()).delete(any());
    }

    // =========================================================================
    // [F7-BACK-02] TESTES DE BAIXA E ALERTA DE ESTOQUE MÍNIMO (UC05.1)
    // =========================================================================

    @Test
    @DisplayName("[F7-BACK-02] Deve realizar baixa no estoque quando saldo for suficiente e não atingir mínimo")
    void deveRealizarBaixaComSucessoSemAtingirMinimo() {
        ItemEstoque item = ItemEstoque.builder()
                .id(1L)
                .nome("Cartucho 03RL")
                .categoria("AGULHAS")
                .quantidade(30)
                .quantidadeMinima(10)
                .unidadeMedida("unidades")
                .build();

        when(estoqueRepository.findById(1L)).thenReturn(Optional.of(item));
        when(estoqueRepository.save(any(ItemEstoque.class))).thenAnswer(i -> i.getArgument(0));

        BaixaEstoqueResponse response = estoqueService.darBaixa(1L, 5, "Sessão cliente 12", 100L);

        assertThat(response.getQuantidadeBaixada()).isEqualTo(5);
        assertThat(response.getSaldoAnterior()).isEqualTo(30);
        assertThat(response.getSaldoAtual()).isEqualTo(25);
        assertThat(response.isAtingiuEstoqueMinimo()).isFalse();
        assertThat(response.isAlertaDisparado()).isFalse();
        assertThat(response.getAlerta()).isNull();
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    @DisplayName("[F7-BACK-02] Deve disparar notificação ao administrador quando saldo atingir exatamente o estoque mínimo")
    void deveDispararAlertaAoAtingirEstoqueMinimoExato() {
        ItemEstoque item = ItemEstoque.builder()
                .id(2L)
                .nome("Tinta Dynamic Black")
                .categoria("TINTAS")
                .quantidade(15)
                .quantidadeMinima(10)
                .unidadeMedida("frascos")
                .build();

        when(estoqueRepository.findById(2L)).thenReturn(Optional.of(item));
        when(estoqueRepository.save(any(ItemEstoque.class))).thenAnswer(i -> i.getArgument(0));
        when(usuarioRepository.findByPerfilAndAtivoTrue("ADMIN")).thenReturn(List.of(adminMock));

        BaixaEstoqueResponse response = estoqueService.darBaixa(2L, 5, "Consumo diário", null);

        assertThat(response.getSaldoAtual()).isEqualTo(10);
        assertThat(response.isAtingiuEstoqueMinimo()).isTrue();
        assertThat(response.isAlertaDisparado()).isTrue();
        assertThat(response.getAlerta()).isNotNull();
        assertThat(response.getAlerta().getNomeItem()).isEqualTo("Tinta Dynamic Black");
        assertThat(response.getAlerta().getQuantidadeAtual()).isEqualTo(10);
        assertThat(response.getAlerta().getQuantidadeMinima()).isEqualTo(10);
        assertThat(response.getAlerta().getAdministradoresNotificados()).contains("Admin Studio (admin@tattooflow.com)");

        assertThat(estoqueService.listarHistoricoAlertas()).hasSize(1);
    }

    @Test
    @DisplayName("[F7-BACK-02] Deve disparar alerta quando saldo ficar abaixo do limite mínimo")
    void deveDispararAlertaQuandoSaldoFicarAbaixoDoMinimo() {
        ItemEstoque item = ItemEstoque.builder()
                .id(3L)
                .nome("Luvas Nitrílicas M")
                .categoria("LUVAS")
                .quantidade(20)
                .quantidadeMinima(15)
                .unidadeMedida("caixas")
                .build();

        when(estoqueRepository.findById(3L)).thenReturn(Optional.of(item));
        when(estoqueRepository.save(any(ItemEstoque.class))).thenAnswer(i -> i.getArgument(0));
        when(usuarioRepository.findByPerfilAndAtivoTrue("ADMIN")).thenReturn(List.of(adminMock));

        BaixaEstoqueResponse response = estoqueService.darBaixa(3L, 8, "Uso em procedimentos", null);

        assertThat(response.getSaldoAtual()).isEqualTo(12); // 12 < 15
        assertThat(response.isAtingiuEstoqueMinimo()).isTrue();
        assertThat(response.isAlertaDisparado()).isTrue();
    }

    @Test
    @DisplayName("[F7-BACK-02] Deve rejeitar baixa quando quantidade for maior que o saldo disponível")
    void deveRejeitarBaixaComSaldoInsuficiente() {
        ItemEstoque item = ItemEstoque.builder()
                .id(4L)
                .nome("Transfer Stencil")
                .quantidade(3)
                .quantidadeMinima(2)
                .build();

        when(estoqueRepository.findById(4L)).thenReturn(Optional.of(item));

        BusinessException ex = assertThrows(BusinessException.class, () -> estoqueService.darBaixa(4L, 5, "Consumo", null));
        assertThat(ex.getMessage()).contains("Saldo insuficiente em estoque");
        verify(estoqueRepository, never()).save(any());
    }

    @Test
    @DisplayName("[F7-BACK-02] Deve rejeitar baixa com quantidade zero ou negativa")
    void deveRejeitarBaixaComQuantidadeZeroOuNegativa() {
        ItemEstoque item = ItemEstoque.builder().id(5L).quantidade(10).quantidadeMinima(2).build();
        when(estoqueRepository.findById(5L)).thenReturn(Optional.of(item));

        assertThrows(BusinessException.class, () -> estoqueService.darBaixa(5L, 0, "Teste", null));
        assertThrows(BusinessException.class, () -> estoqueService.darBaixa(5L, -2, "Teste", null));
        assertThrows(BusinessException.class, () -> estoqueService.darBaixa(5L, null, "Teste", null));
    }

    @Test
    @DisplayName("[F7-BACK-02] Deve alocar materiais em lote e notificar apenas itens que atingirem o mínimo")
    void deveAlocarMateriaisEmLote() {
        ItemEstoque agulha = ItemEstoque.builder().id(101L).nome("Agulha 07MG").quantidade(20).quantidadeMinima(5).build();
        ItemEstoque tinta = ItemEstoque.builder().id(102L).nome("Tinta Branca").quantidade(3).quantidadeMinima(2).build();

        when(estoqueRepository.findById(101L)).thenReturn(Optional.of(agulha));
        when(estoqueRepository.findById(102L)).thenReturn(Optional.of(tinta));
        when(estoqueRepository.save(any(ItemEstoque.class))).thenAnswer(i -> i.getArgument(0));
        when(usuarioRepository.findByPerfilAndAtivoTrue("ADMIN")).thenReturn(List.of(adminMock));

        AlocacaoEstoqueRequest request = AlocacaoEstoqueRequest.builder()
                .sessionId("sessao-99")
                .items(List.of(
                        new AlocacaoItemPayload(101L, "Agulha 07MG", 2), // saldo: 18 (min: 5) -> sem alerta
                        new AlocacaoItemPayload(102L, "Tinta Branca", 2)  // saldo: 1 (min: 2) -> com alerta
                ))
                .build();

        List<BaixaEstoqueResponse> respostas = estoqueService.alocarMateriais(request);

        assertThat(respostas).hasSize(2);
        assertThat(respostas.get(0).isAtingiuEstoqueMinimo()).isFalse();
        assertThat(respostas.get(1).isAtingiuEstoqueMinimo()).isTrue();
        assertThat(estoqueService.listarHistoricoAlertas()).hasSize(1);
    }
}

