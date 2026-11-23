<template>
  <div class="documento-impresso bg-white text-[#111111] p-6 sm:p-8 font-sans max-w-[800px] mx-auto text-[10pt] leading-[1.35] shadow-md border border-neutral-300 print:shadow-none print:border-none print:p-0 print:m-0 print:max-w-none">
    <!-- CABEÇALHO DO ESTÚDIO -->
    <header class="border-b-2 border-[#111111] pb-3 mb-4 text-center">
      <h1 class="text-[16pt] font-bold tracking-wider uppercase mb-0.5">
        TATTOOFLOW STUDIO
      </h1>
      <h2 class="text-[11pt] font-semibold tracking-wide uppercase text-neutral-800">
        Dossiê Artístico &amp; Prontuário Técnico do Cliente
      </h2>
      <p class="text-[8pt] text-neutral-600 mt-1">
        Acervo Histórico de Procedimentos, Curadoria Dérmica e Registro Sanitário Notarial
      </p>
      <div class="flex justify-between items-center text-[8.5pt] font-mono mt-2 pt-1 border-t border-neutral-300">
        <span>IDENTIFICADOR: <strong>{{ dossier?.dossierNumber || '#0001' }}</strong></span>
        <span>EMISSÃO: <strong>{{ formattedDate }}</strong></span>
      </div>
    </header>

    <!-- 1. IDENTIFICAÇÃO DO CLIENTE -->
    <section class="mb-3.5 avoid-break">
      <h3 class="text-[9pt] font-bold uppercase bg-neutral-100 border border-neutral-400 px-2 py-1 mb-1.5">
        1. Identificação Cadastral do Cliente
      </h3>
      <table class="w-full border-collapse border border-neutral-400 text-[9pt]">
        <tbody>
          <tr>
            <td class="border border-neutral-400 p-1.5 w-1/2">
              <span class="block text-[7.5pt] uppercase text-neutral-600 font-bold">Nome do(a) Cliente:</span>
              <strong class="text-[9.5pt]">{{ dossier?.name || 'Cliente não identificado' }}</strong>
            </td>
            <td class="border border-neutral-400 p-1.5 w-1/2">
              <span class="block text-[7.5pt] uppercase text-neutral-600 font-bold">CPF / Documento:</span>
              <span>{{ dossier?.cpf || 'Não informado' }}</span>
            </td>
          </tr>
          <tr>
            <td class="border border-neutral-400 p-1.5">
              <span class="block text-[7.5pt] uppercase text-neutral-600 font-bold">Telefone / WhatsApp:</span>
              <span>{{ dossier?.phone || 'Não informado' }}</span>
            </td>
            <td class="border border-neutral-400 p-1.5">
              <span class="block text-[7.5pt] uppercase text-neutral-600 font-bold">Volume no Acervo:</span>
              <span>
                <strong>{{ dossier?.worksCount || (dossier?.artworks || []).length || 0 }}</strong> Obra(s) registrada(s) •
                <strong>{{ dossier?.scheduledWorksCount || 0 }}</strong> Agendada(s)
              </span>
            </td>
          </tr>
        </tbody>
      </table>
    </section>

    <!-- 2. PERFIL DÉRMICO E ANAMNESE -->
    <section class="mb-3.5 avoid-break">
      <h3 class="text-[9pt] font-bold uppercase bg-neutral-100 border border-neutral-400 px-2 py-1 mb-1.5">
        2. Perfil Dérmico &amp; Curadoria Sanitária
      </h3>
      <table class="w-full border-collapse border border-neutral-400 text-[8.5pt]">
        <tbody>
          <tr>
            <td class="border border-neutral-400 p-1.5 w-1/2">
              <span class="block text-[7.5pt] uppercase text-neutral-600 font-bold">Fototipo Dérmico / Fitzpatrick:</span>
              <strong>{{ dossier?.dermalProfile?.fitzpatrick || 'PERFIL DÉRMICO REGISTRADO' }}</strong>
              <div class="text-[8pt] text-neutral-700 mt-0.5">
                {{ dossier?.dermalProfile?.description || 'Histórico clínico e dermatológico cadastrado no prontuário do atelier.' }}
              </div>
            </td>
            <td class="border border-neutral-400 p-1.5 w-1/2">
              <span class="block text-[7.5pt] uppercase text-neutral-600 font-bold">Situação da Anamnese:</span>
              <strong>{{ dossier?.anamneseStatus?.title || 'ANAMNESE VÁLIDA' }} [{{ dossier?.anamneseStatus?.badge || 'APROVADA' }}]</strong>
              <div class="text-[8pt] text-neutral-700 mt-0.5">
                {{ dossier?.anamneseStatus?.notes || 'Termo de consentimento e declaração de saúde devidamente auditados.' }}
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </section>

    <!-- 3. HISTÓRICO DE OBRAS & SESSÕES -->
    <section class="mb-3.5 avoid-break">
      <h3 class="text-[9pt] font-bold uppercase bg-neutral-100 border border-neutral-400 px-2 py-1 mb-1.5 flex justify-between items-center">
        <span>3. Acervo de Obras, Sessões &amp; Registros Notariais</span>
        <span class="text-[7.5pt] font-normal normal-case text-neutral-600">
          ({{ (dossier?.artworks || []).length }} registro(s) arquivado(s))
        </span>
      </h3>
      <table class="w-full border-collapse border border-neutral-400 text-[8.5pt]">
        <thead>
          <tr class="bg-neutral-100">
            <th class="border border-neutral-400 p-1 text-left w-[30%]">Obra / Descrição do Conceito</th>
            <th class="border border-neutral-400 p-1 text-left w-[20%]">Data &amp; Duração</th>
            <th class="border border-neutral-400 p-1 text-left w-[22%]">Tatuador(a)</th>
            <th class="border border-neutral-400 p-1 text-center w-[14%]">Cicatrização</th>
            <th class="border border-neutral-400 p-1 text-center w-[14%]">Termo Notarial</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="art in (dossier?.artworks || [])" :key="art.id">
            <td class="border border-neutral-400 p-1.5">
              <strong class="text-[#111111]">{{ art.title }}</strong>
              <div class="text-[7.5pt] text-neutral-600 line-clamp-1">{{ art.conceptDescription }}</div>
            </td>
            <td class="border border-neutral-400 p-1.5 text-[8pt]">
              {{ art.dateAndDuration }}
            </td>
            <td class="border border-neutral-400 p-1.5 text-[8pt]">
              {{ art.artistResponsible }}
            </td>
            <td class="border border-neutral-400 p-1 text-center text-[8pt]">
              {{ art.healingStatus || art.badgeStatus || 'Concluída' }}
            </td>
            <td class="border border-neutral-400 p-1 text-center font-mono text-[7.5pt]">
              {{ art.signedTermAvailable ? 'ASSINADO' : 'PENDENTE' }}
            </td>
          </tr>
          <tr v-if="!dossier?.artworks || dossier.artworks.length === 0">
            <td colspan="5" class="border border-neutral-400 p-3 text-center text-neutral-500 italic text-[8.5pt]">
              Nenhuma sessão ou obra arquivada no histórico deste cliente até o momento.
            </td>
          </tr>
        </tbody>
      </table>
    </section>

    <!-- 4. DIRETRIZES DE PRÉ E PÓS-CUIDADO -->
    <section class="mb-3.5 avoid-break">
      <h3 class="text-[9pt] font-bold uppercase bg-neutral-100 border border-neutral-400 px-2 py-1 mb-1.5">
        4. Protocolos Sanitários de Pré &amp; Pós-Cuidado
      </h3>
      <table class="w-full border-collapse border border-neutral-400 text-[8.5pt]">
        <tbody>
          <tr>
            <td class="border border-neutral-400 p-1.5 w-1/2">
              <span class="block text-[7.5pt] uppercase text-neutral-600 font-bold mb-0.5">
                Protocolo de Pré-Cuidado (48h antes):
              </span>
              <p class="text-[8pt] text-neutral-800 leading-relaxed italic">
                "{{ dossier?.preMessageText || 'Recomenda-se boa hidratação dérmica, evitar exposição solar direta e não ingerir bebidas alcoólicas nas 24h anteriores ao procedimento.' }}"
              </p>
            </td>
            <td class="border border-neutral-400 p-1.5 w-1/2">
              <span class="block text-[7.5pt] uppercase text-neutral-600 font-bold mb-0.5">
                Protocolo de Pós-Cuidado &amp; Cicatrização (D+15):
              </span>
              <p class="text-[8pt] text-neutral-800 leading-relaxed italic">
                "{{ dossier?.posMessageText || 'Solicitação de fotos em luz natural para avaliação de cicatrização e regeneração epitelial aos 15 dias pós-sessão.' }}"
              </p>
            </td>
          </tr>
        </tbody>
      </table>
    </section>

    <!-- 5. AUTENTICAÇÃO E RESPONSÁVEL TÉCNICO -->
    <section class="avoid-break mt-4">
      <div class="text-right text-[8.5pt] mb-4">
        <span>São Paulo - SP, {{ formattedDateExtended }}</span>
      </div>

      <div class="grid grid-cols-2 gap-6 pt-2">
        <div class="text-center flex flex-col justify-end min-h-[60px]">
          <div class="border-t border-neutral-600 pt-1">
            <div class="font-bold text-[9pt]">{{ dossier?.name || 'Cliente' }}</div>
            <div class="text-[7.5pt] text-neutral-600">Cliente Titular do Prontuário</div>
          </div>
        </div>

        <div class="text-center flex flex-col justify-end min-h-[60px]">
          <div class="border-t border-neutral-600 pt-1">
            <div class="font-bold text-[9pt]">Direção Técnica / Curadoria</div>
            <div class="text-[7.5pt] text-neutral-600">Atelier TattooFlow • Registro Geral de Acervo</div>
          </div>
        </div>
      </div>
    </section>

    <!-- RODAPÉ NOTARIAL -->
    <footer class="mt-6 pt-2 border-t border-neutral-300 text-center text-[7.5pt] text-neutral-500">
      TattooFlow Studio Management • Dossiê Técnico Confidencial • Proteção de Dados LGPD • Validade Documental.
    </footer>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { ClientDossier } from '@/stores/clients';

const props = defineProps<{
  dossier: ClientDossier | null;
}>();

const formattedDate = computed(() => {
  return new Date().toLocaleDateString('pt-BR');
});

const formattedDateExtended = computed(() => {
  const d = new Date();
  const dia = d.getDate();
  const mes = d.toLocaleDateString('pt-BR', { month: 'long' });
  const ano = d.getFullYear();
  return `${dia} de ${mes} de ${ano}`;
});
</script>

<style scoped>
@media print {
  .documento-impresso {
    box-shadow: none !important;
    border: none !important;
    padding: 0 !important;
    margin: 0 !important;
    max-width: 100% !important;
  }
}
</style>
