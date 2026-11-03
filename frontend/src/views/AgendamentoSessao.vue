<template>
  <ion-page>
    <ion-content class="bg-[#09090b] text-[#f2ebd9]" fullscreen>
      <main class="min-h-screen bg-[#09090b] max-w-[560px] mx-auto px-5 pt-5 pb-28">
        <button @click="router.push('/agenda')" class="flex items-center gap-2 text-sm text-[#c9a86a] py-2 mb-6"><ArrowLeft class="w-4 h-4" /> Voltar à agenda</button>
        <p class="text-[10px] tracking-[2px] text-[#c9a86a] uppercase">Sessões do estúdio</p>
        <h1 class="font-playfair text-3xl mt-2">Novo agendamento</h1>
        <p class="text-sm text-[#a39e93] mt-2 mb-6">Escolha o cliente e reserve o horário da sessão.</p>

        <p v-if="carregando" role="status" class="text-sm text-[#a39e93] py-5">Carregando clientes e tatuadores...</p>
        <div v-if="erroCarregamento" role="alert" class="rounded border border-red-300/30 p-4 mb-5 text-sm text-[#ef8b83]">
          {{ erroCarregamento }}
          <button @click="carregar" class="block underline mt-2">Tentar novamente</button>
        </div>

        <form v-if="!carregando && !erroCarregamento" @submit.prevent="salvar" class="space-y-5">
          <fieldset :disabled="salvando || cadastrandoCliente || cadastrandoTatuador" class="space-y-5 disabled:opacity-60">
            <section class="bg-[#141416] border border-white/10 rounded-lg p-4 space-y-4">
              <div class="flex items-center justify-between gap-2">
                <label for="cliente-agenda" class="text-xs text-[#e6c383] uppercase tracking-wider">Cliente</label>
                <button type="button" @click="novoCliente = !novoCliente" class="text-xs text-[#c9a86a] underline">{{ novoCliente ? 'Usar cliente cadastrado' : 'Cadastrar cliente' }}</button>
              </div>
              <div v-if="novoCliente" class="space-y-3 border-b border-white/10 pb-4">
                <label class="block text-xs text-[#a39e93]">Nome do cliente<input v-model="nomeCliente" maxlength="255" class="campo mt-2" autocomplete="name" /></label>
                <label class="block text-xs text-[#a39e93]">WhatsApp com código do país e DDD<input v-model="telefoneCliente" type="tel" class="campo mt-2" placeholder="5511999999999" autocomplete="tel" /></label>
                <p v-if="erroCliente" role="alert" class="text-xs text-[#ef8b83]">{{ erroCliente }}</p>
                <button type="button" @click="cadastrarCliente" class="border border-[#c9a86a]/50 text-[#e6c383] rounded px-3 py-2 text-sm">{{ cadastrandoCliente ? 'Salvando cliente...' : 'Salvar cliente' }}</button>
              </div>
              <select id="cliente-agenda" v-model="form.clienteId" required class="campo">
                <option value="" disabled>Selecione um cliente</option>
                <option v-for="cliente in clientes" :key="cliente.id" :value="cliente.id">{{ cliente.nome }}{{ cliente.telefone ? ` · ${cliente.telefone}` : '' }}</option>
              </select>
              <p v-if="!clientes.length" class="text-xs text-[#a39e93]">Cadastre o primeiro cliente acima para continuar.</p>
              <p v-else-if="clienteSelecionado && !clienteSelecionado.telefone" class="text-xs text-[#e6c383]">Este cliente não tem telefone cadastrado. Os cuidados automáticos precisam de um telefone.</p>
              <div class="flex items-center justify-between gap-2">
                <label for="tatuador-agenda" class="text-xs text-[#e6c383] uppercase tracking-wider">Tatuador</label>
                <button v-if="auth.user.role === 'admin'" type="button" @click="novoTatuador = !novoTatuador" class="text-xs text-[#c9a86a] underline">{{ novoTatuador ? 'Usar tatuador cadastrado' : 'Cadastrar tatuador' }}</button>
              </div>
              <div v-if="novoTatuador" class="space-y-3 border-b border-white/10 pb-4">
                <label class="block text-xs text-[#a39e93]">Nome do tatuador<input v-model="dadosTatuador.nome" maxlength="255" class="campo mt-2" /></label>
                <label class="block text-xs text-[#a39e93]">E-mail<input v-model="dadosTatuador.email" type="email" maxlength="255" class="campo mt-2" autocomplete="off" /></label>
                <label class="block text-xs text-[#a39e93]">PIN de acesso (4 a 6 dígitos)<input v-model="dadosTatuador.pin" type="password" inputmode="numeric" maxlength="6" class="campo mt-2" autocomplete="new-password" /></label>
                <label class="block text-xs text-[#a39e93]">ID do aparelho autorizado<input v-model="dadosTatuador.aparelhoId" maxlength="255" class="campo mt-2" /></label>
                <p v-if="erroTatuador" role="alert" class="text-xs text-[#ef8b83]">{{ erroTatuador }}</p>
                <button type="button" @click="cadastrarTatuador" class="border border-[#c9a86a]/50 text-[#e6c383] rounded px-3 py-2 text-sm">{{ cadastrandoTatuador ? 'Salvando tatuador...' : 'Salvar tatuador' }}</button>
              </div>
              <select id="tatuador-agenda" v-model="form.tatuadorId" required class="campo">
                <option value="" disabled>Selecione um tatuador</option>
                <option v-for="tatuador in tatuadores" :key="tatuador.id" :value="tatuador.id">{{ tatuador.nome }}</option>
              </select>
              <p v-if="!tatuadores.length" role="status" class="text-xs text-[#e6c383]">Não há tatuadores ativos cadastrados. Peça à administração para cadastrar um usuário com perfil TATUADOR.</p>
            </section>

            <section class="bg-[#141416] border border-white/10 rounded-lg p-4 space-y-4">
              <h2 class="font-playfair text-xl">Data e horário</h2>
              <div class="grid grid-cols-2 gap-3">
                <label class="text-xs text-[#a39e93]">Data<input v-model="form.data" required type="date" class="campo mt-2" /></label>
                <label class="text-xs text-[#a39e93]">Horário<input v-model="form.hora" required type="time" class="campo mt-2" /></label>
              </div>
              <label class="block text-xs text-[#a39e93]">Duração em minutos<input v-model.number="form.duracaoMinutos" required type="number" min="1" max="1440" step="1" class="campo mt-2" /></label>
              <p v-if="termino" class="text-xs text-[#c9a86a]">Término previsto: {{ termino }}</p>
              <label for="status-agendamento" class="block text-xs text-[#a39e93]">Status</label>
              <select id="status-agendamento" v-model="form.status" class="campo"><option value="AGENDADO">Agendado</option><option value="EM_ANDAMENTO">Em andamento</option><option value="CONCLUIDO">Concluído</option></select>
            </section>

            <section class="bg-[#141416] border border-white/10 rounded-lg p-4 space-y-4">
              <label class="block text-xs text-[#a39e93]">Descrição da sessão<textarea v-model="form.descricaoSessao" maxlength="255" rows="3" class="campo mt-2" placeholder="Projeto, região do corpo ou observações" /></label>
              <label class="block text-xs text-[#a39e93]">Valor estimado (R$) · opcional<input v-model="form.valorEstimado" type="number" min="0" step="0.01" class="campo mt-2" placeholder="0,00" /></label>
            </section>
            <p class="text-xs leading-relaxed text-[#a39e93]">Os cuidados pré-sessão são enviados antes do início. Os cuidados pós-sessão exigem status concluído e respeitam o prazo configurado após o término previsto.</p>
            <p v-if="erro" role="alert" class="text-sm text-[#ef8b83] border border-red-300/30 rounded p-3">{{ erro }}</p>
            <button type="submit" :disabled="!tatuadores.length || !clientes.length" class="w-full flex items-center justify-center gap-2 bg-[#c9a86a] text-[#09090b] rounded py-3.5 font-bold text-sm disabled:opacity-40"><CalendarCheck class="w-4 h-4" />{{ salvando ? 'Salvando...' : 'Salvar agendamento' }}</button>
          </fieldset>
        </form>
      </main>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { IonPage, IonContent, onIonViewWillEnter } from '@ionic/vue';
import { ArrowLeft, CalendarCheck } from '@lucide/vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { criarClienteAgenda, criarTatuadorAgenda, listarClientesAgenda, listarUsuariosAgenda, salvarAgendamento,
  type ClienteAgenda, type UsuarioAgenda, type StatusAgendamento } from '@/api/agendamentos';

const router = useRouter();
const route = useRoute();
const auth = useAuthStore();
const clientes = ref<ClienteAgenda[]>([]);
const tatuadores = ref<UsuarioAgenda[]>([]);
const carregando = ref(false);
const salvando = ref(false);
const erroCarregamento = ref('');
const erro = ref('');
const novoCliente = ref(false);
const cadastrandoCliente = ref(false);
const nomeCliente = ref('');
const telefoneCliente = ref('');
const erroCliente = ref('');
const novoTatuador = ref(false);
const cadastrandoTatuador = ref(false);
const erroTatuador = ref('');
const dadosTatuador = reactive({ nome: '', email: '', pin: '', aparelhoId: '' });
const form = reactive({ clienteId: '' as number | '', tatuadorId: '' as number | '', data: '', hora: '', duracaoMinutos: 60,
  status: 'AGENDADO' as StatusAgendamento, descricaoSessao: '', valorEstimado: '' });

const clienteSelecionado = computed(() => clientes.value.find((cliente) => cliente.id === form.clienteId));
const termino = computed(() => {
  if (!form.data || !form.hora || form.duracaoMinutos <= 0) return '';
  const fim = new Date(`${form.data}T${form.hora}:00`);
  fim.setMinutes(fim.getMinutes() + Number(form.duracaoMinutos));
  return Number.isNaN(fim.getTime()) ? '' : fim.toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' });
});

onIonViewWillEnter(() => {
  const agora = new Date();
  agora.setHours(agora.getHours() + 1, 0, 0, 0);
  Object.assign(form, { clienteId: '', tatuadorId: '', data: agora.toLocaleDateString('sv-SE'),
    hora: `${String(agora.getHours()).padStart(2, '0')}:00`, duracaoMinutos: 60, status: 'AGENDADO', descricaoSessao: '', valorEstimado: '' });
  erro.value = '';
  novoCliente.value = false;
  novoTatuador.value = false;
  Object.assign(dadosTatuador, { nome: '', email: '', pin: '', aparelhoId: '' });
  void carregar();
});

async function carregar() {
  carregando.value = true;
  erroCarregamento.value = '';
  try {
    const [pessoas, equipe] = await Promise.all([listarClientesAgenda(), listarUsuariosAgenda()]);
    clientes.value = pessoas;
    tatuadores.value = equipe.filter((usuario) => usuario.perfil === 'TATUADOR' && usuario.ativo);
    const clientId = Number(route.query.clientId);
    if (pessoas.some((cliente) => cliente.id === clientId)) form.clienteId = clientId;
    const meuId = Number(auth.user.id);
    if (tatuadores.value.some((usuario) => usuario.id === meuId)) form.tatuadorId = meuId;
    else if (tatuadores.value.length === 1) form.tatuadorId = tatuadores.value[0]!.id;
  } catch (err) {
    erroCarregamento.value = err instanceof Error ? err.message : 'Não foi possível carregar as opções.';
  } finally {
    carregando.value = false;
  }
}

async function cadastrarCliente() {
  if (cadastrandoCliente.value) return;
  erroCliente.value = '';
  const telefone = telefoneCliente.value.replace(/\D/g, '');
  if (!nomeCliente.value.trim() || !/^[1-9]\d{9,14}$/.test(telefone)) {
    erroCliente.value = 'Informe o nome e o WhatsApp com código do país e DDD (10 a 15 dígitos).';
    return;
  }
  cadastrandoCliente.value = true;
  try {
    const cliente = await criarClienteAgenda(nomeCliente.value.trim(), telefone);
    clientes.value.push(cliente);
    form.clienteId = cliente.id;
    novoCliente.value = false;
    nomeCliente.value = '';
    telefoneCliente.value = '';
  } catch (err) {
    erroCliente.value = err instanceof Error ? err.message : 'Não foi possível cadastrar o cliente.';
  } finally {
    cadastrandoCliente.value = false;
  }
}

async function salvar() {
  if (salvando.value || cadastrandoCliente.value || cadastrandoTatuador.value) return;
  erro.value = '';
  if (!form.clienteId || !form.tatuadorId || !form.data || !form.hora || !Number.isInteger(form.duracaoMinutos) || form.duracaoMinutos < 1 || form.duracaoMinutos > 1440) {
    erro.value = 'Selecione cliente, tatuador, data e horário, com duração entre 1 e 1440 minutos.';
    return;
  }
  salvando.value = true;
  try {
    const agendamento = await salvarAgendamento({ clienteId: form.clienteId, tatuadorId: form.tatuadorId,
      dataHora: `${form.data}T${form.hora}:00`, duracaoMinutos: form.duracaoMinutos, status: form.status,
      descricaoSessao: form.descricaoSessao.trim(), valorEstimado: form.valorEstimado === '' ? null : Number(form.valorEstimado) });
    await router.push({ path: '/agenda', query: { criado: agendamento.id } });
  } catch (err) {
    erro.value = err instanceof Error ? err.message : 'Não foi possível salvar o agendamento.';
  } finally {
    salvando.value = false;
  }
}

async function cadastrarTatuador() {
  if (cadastrandoTatuador.value) return;
  erroTatuador.value = '';
  if (!dadosTatuador.nome.trim() || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(dadosTatuador.email.trim())
      || !/^\d{4,6}$/.test(dadosTatuador.pin) || !dadosTatuador.aparelhoId.trim()) {
    erroTatuador.value = 'Informe nome, e-mail, PIN de 4 a 6 dígitos e o ID do aparelho autorizado.';
    return;
  }
  cadastrandoTatuador.value = true;
  try {
    const tatuador = await criarTatuadorAgenda({ nome: dadosTatuador.nome.trim(), email: dadosTatuador.email.trim(),
      pin: dadosTatuador.pin, aparelhoId: dadosTatuador.aparelhoId.trim() });
    tatuadores.value.push(tatuador);
    form.tatuadorId = tatuador.id;
    novoTatuador.value = false;
    Object.assign(dadosTatuador, { nome: '', email: '', pin: '', aparelhoId: '' });
  } catch (err) {
    erroTatuador.value = err instanceof Error ? err.message : 'Não foi possível cadastrar o tatuador.';
  } finally {
    cadastrandoTatuador.value = false;
  }
}
</script>

<style scoped>
button { min-height: 40px; padding: 8px 12px; }
.campo { display: block; width: 100%; min-width: 0; border: 1px solid rgba(201, 168, 106, 0.22); border-radius: 4px; background: #09090b; color: #f2ebd9; padding: 11px 12px; font-size: 14px; color-scheme: dark; }
.campo:focus { outline: 2px solid #c9a86a; outline-offset: 2px; }
</style>
