<template>
  <div class="documento-impresso bg-white text-[#111111] p-6 sm:p-8 font-sans max-w-[800px] mx-auto text-[10pt] leading-[1.35] shadow-md border border-neutral-300 print:shadow-none print:border-none print:p-0 print:m-0 print:max-w-none">
    <!-- CABEÇALHO DO ESTÚDIO -->
    <header class="border-b-2 border-[#111111] pb-3 mb-4 text-center">
      <h1 class="text-[16pt] font-bold tracking-wider uppercase mb-0.5">
        TATTOOFLOW STUDIO
      </h1>
      <h2 class="text-[11pt] font-semibold tracking-wide uppercase text-neutral-800">
        Ficha Clínica de Anamnese &amp; Termo de Consentimento Notarial
      </h2>
      <p class="text-[8pt] text-neutral-600 mt-1">
        Conformidade Sanitária ANVISA (RDC) • Procedimento de Pigmentação Artificial / Body Art
      </p>
      <div class="flex justify-between items-center text-[8.5pt] font-mono mt-2 pt-1 border-t border-neutral-300">
        <span>PROTOCOLO / DOC: <strong>{{ termNumber || 'DOC-PENDENTE' }}</strong></span>
        <span>DATA DE EMISSÃO: <strong>{{ formattedDate }}</strong></span>
      </div>
    </header>

    <!-- 1. DADOS DO CLIENTE E DO PROFISSIONAL -->
    <section class="mb-3.5 avoid-break">
      <h3 class="text-[9pt] font-bold uppercase bg-neutral-100 border border-neutral-400 px-2 py-1 mb-1.5">
        1. Identificação das Partes &amp; Procedimento
      </h3>
      <table class="w-full border-collapse border border-neutral-400 text-[9pt]">
        <thead class="sr-only">
          <tr>
            <th scope="col">Campo</th>
            <th scope="col">Valor</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td class="border border-neutral-400 p-1.5 w-1/2">
              <span class="block text-[7.5pt] uppercase text-neutral-600 font-bold">Cliente Solicitante:</span>
              <strong class="text-[9.5pt]">{{ clientName || '________________________________________________' }}</strong>
            </td>
            <td class="border border-neutral-400 p-1.5 w-1/2">
              <span class="block text-[7.5pt] uppercase text-neutral-600 font-bold">CPF / Documento:</span>
              <span>{{ clientCpf || '____________________________________' }}</span>
            </td>
          </tr>
          <tr>
            <td class="border border-neutral-400 p-1.5">
              <span class="block text-[7.5pt] uppercase text-neutral-600 font-bold">Telefone / WhatsApp:</span>
              <span>{{ clientPhone || '____________________________________' }}</span>
            </td>
            <td class="border border-neutral-400 p-1.5">
              <span class="block text-[7.5pt] uppercase text-neutral-600 font-bold">Data de Nasc. / Idade:</span>
              <span>____________________________________</span>
            </td>
          </tr>
          <tr>
            <td class="border border-neutral-400 p-1.5">
              <span class="block text-[7.5pt] uppercase text-neutral-600 font-bold">Tatuador(a) Responsável:</span>
              <strong class="text-[9.5pt]">{{ artistName || '________________________________________________' }}</strong>
            </td>
            <td class="border border-neutral-400 p-1.5">
              <span class="block text-[7.5pt] uppercase text-neutral-600 font-bold">Bancada / Posto de Trabalho:</span>
              <span>{{ artistStation || 'Bancada do Atelier' }}</span>
            </td>
          </tr>
          <tr>
            <td class="border border-neutral-400 p-1.5" colspan="2">
              <span class="block text-[7.5pt] uppercase text-neutral-600 font-bold">Projeto / Estilo / Local Anatômico:</span>
              <span>
                {{ projectTitle || 'Projeto Artístico' }}
                <span v-if="style"> • Estilo: {{ style }}</span>
                <span v-if="conceptDescription"> • Local: {{ conceptDescription }}</span>
              </span>
            </td>
          </tr>
        </tbody>
      </table>
    </section>

    <!-- 2. QUESTIONÁRIO DE ANAMNESE E HISTÓRICO DE SAÚDE -->
    <section class="mb-3.5 avoid-break">
      <h3 class="text-[9pt] font-bold uppercase bg-neutral-100 border border-neutral-400 px-2 py-1 mb-1.5 flex justify-between items-center">
        <span>2. Avaliação de Anamnese &amp; Saúde Dérmica</span>
        <span class="text-[7.5pt] font-normal normal-case text-neutral-600">(Preenchimento Obrigatório)</span>
      </h3>
      <table class="w-full border-collapse border border-neutral-400 text-[8.5pt]">
        <thead>
          <tr class="bg-neutral-100">
            <th class="border border-neutral-400 p-1 text-left w-[62%]">Condição Clínica / Investigação Sanitária</th>
            <th class="border border-neutral-400 p-1 text-center w-[18%]">Resposta</th>
            <th class="border border-neutral-400 p-1 text-left w-[20%]">Detalhes / Observações</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="q in fullQuestions" :key="q.id">
            <td class="border border-neutral-400 p-1.5">
              <div class="font-medium text-[#111111]">{{ q.title }}</div>
              <div v-if="q.subtitle" class="text-[7.5pt] text-neutral-600">{{ q.subtitle }}</div>
            </td>
            <td class="border border-neutral-400 p-1 text-center font-mono">
              <span :class="q.value === true ? 'font-bold' : ''">[ {{ q.value === true ? 'X' : ' ' }} ] SIM</span>
              &nbsp;&nbsp;
              <span :class="q.value === false ? 'font-bold' : ''">[ {{ q.value === false ? 'X' : ' ' }} ] NÃO</span>
            </td>
            <td class="border border-neutral-400 p-1.5 text-[8pt]">
              {{ q.detailText || (q.value === true ? 'Informado no relato' : '—') }}
            </td>
          </tr>
        </tbody>
      </table>

      <!-- Campo para observações médicas adicionais na bancada -->
      <div class="border border-neutral-400 border-t-0 p-2 text-[8pt]">
        <div class="font-bold text-neutral-700 uppercase text-[7.5pt] mb-1">
          Observações Clínicas Adicionais / Alergias Específicas / Histórico Relevante:
        </div>
        <div class="border-b border-dashed border-neutral-400 h-4"></div>
        <div class="border-b border-dashed border-neutral-400 h-4"></div>
      </div>
    </section>

    <!-- 3. TERMO DE CONSENTIMENTO E RESPONSABILIDADE JURÍDICA -->
    <section class="mb-3.5 avoid-break text-[8pt] text-justify leading-[1.3] text-neutral-800">
      <h3 class="text-[9pt] font-bold uppercase bg-neutral-100 border border-neutral-400 px-2 py-1 mb-1.5 text-left">
        3. Termo de Consentimento Livre, Esclarecido e Notarial
      </h3>
      <div class="border border-neutral-400 p-2.5 space-y-1.5">
        <p>
          <strong>1. Declaração de Veracidade e Idade:</strong> Declaro, sob fé notarial e as penas civis e penais cabíveis, ter idade igual ou superior a 18 (dezoito) anos ou ser legalmente emancipado(a), e que todas as informações prestadas na ficha de anamnese acima são exatas e verdadeiras, não tendo omitido qualquer histórico de saúde relevante.
        </p>
        <p>
          <strong>2. Ciência de Riscos e Permanência:</strong> Estou plenamente ciente de que a tatuagem artística é um procedimento invasivo e definitivo de introdução de pigmentos na derme por perfuração mecânica. Reconheço a possibilidade de desconforto/dor inerente, vermelhidão, inchaço e processo inflamatório transitório durante o período de cicatrização.
        </p>
        <p>
          <strong>3. Assepsia e Normas Sanitárias ANVISA:</strong> Fui informado(a) e constatei que todo material perfurocortante (agulhas, biqueiras) é 100% estéril, descartável e de uso único, aberto à minha vista e descartado em recipiente rígido apropriado, em estrita conformidade com as normas sanitárias da ANVISA.
        </p>
        <p>
          <strong>4. Compromisso de Pós-Cuidado:</strong> Comprometo-me a seguir rigorosamente as orientações de higienização, curativo, aplicação de emolientes/pomadas e proteção solar indicadas pelo atelier, eximindo o profissional por complicações advindas de negligência no pós-procedimento.
        </p>
      </div>
    </section>

    <!-- 4. LOCAL, DATA E ASSINATURAS -->
    <section class="avoid-break mt-4">
      <div class="text-right text-[8.5pt] mb-4">
        <span>Local e Data: ____________________, {{ formattedDateExtended }}</span>
      </div>

      <div class="grid grid-cols-2 gap-6 pt-2">
        <!-- Assinatura do Cliente -->
        <div class="text-center flex flex-col justify-end min-h-[70px]">
          <div v-if="signatureImage" class="flex justify-center mb-1">
            <img :src="signatureImage" alt="Assinatura Cliente" class="max-h-12 object-contain filter grayscale" />
          </div>
          <div class="border-t border-neutral-600 pt-1">
            <div class="font-bold text-[9pt]">{{ clientName || 'Assinatura do(a) Cliente' }}</div>
            <div class="text-[7.5pt] text-neutral-600">Cliente Solicitante / CPF: {{ clientCpf || '___________________' }}</div>
          </div>
        </div>

        <!-- Assinatura do Tatuador -->
        <div class="text-center flex flex-col justify-end min-h-[70px]">
          <div class="border-t border-neutral-600 pt-1">
            <div class="font-bold text-[9pt]">{{ artistName || 'Assinatura do(a) Tatuador(a)' }}</div>
            <div class="text-[7.5pt] text-neutral-600">Responsável Técnico / Atelier TattooFlow</div>
          </div>
        </div>
      </div>
    </section>

    <!-- RODAPÉ NOTARIAL -->
    <footer class="mt-6 pt-2 border-t border-neutral-300 text-center text-[7.5pt] text-neutral-500">
      Documento Eletrônico TattooFlow • Arquivamento legal de guarda obrigatória por 5 anos (RN06) • Proibida reprodução não autorizada.
    </footer>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

export interface AnamneseQuestionItem {
  id: string;
  title: string;
  subtitle?: string;
  value?: boolean | null;
  detailText?: string;
}

const props = withDefaults(
  defineProps<{
    clientName?: string;
    clientCpf?: string;
    clientPhone?: string;
    artistName?: string;
    artistStation?: string;
    projectTitle?: string;
    style?: string;
    conceptDescription?: string;
    termNumber?: string;
    questions?: AnamneseQuestionItem[];
    signatureImage?: string;
    signedAt?: string;
  }>(),
  {
    clientName: '',
    clientCpf: '',
    clientPhone: '',
    artistName: '',
    artistStation: '',
    projectTitle: '',
    style: '',
    conceptDescription: '',
    termNumber: '',
    questions: () => [],
    signatureImage: '',
    signedAt: '',
  }
);

// Fallback padrão para garantir que o questionário sempre tenha o padrão sanitário completo
const fullQuestions = computed(() => {
  const defaults: AnamneseQuestionItem[] = [
    {
      id: 'alergias',
      title: 'Alergia a pigmentos, látex, cosméticos ou fitas adesivas',
      subtitle: 'Histórico de reações dérmicas a tintas ou derivados',
      value: null,
    },
    {
      id: 'anticoagulantes',
      title: 'Uso de anticoagulantes, antiagregantes ou aspirina',
      subtitle: 'Medicamentos que interferem no tempo de coagulação',
      value: null,
    },
    {
      id: 'queloides',
      title: 'Histórico de queloides ou cicatrização hipertrófica',
      subtitle: 'Tendência da pele a produzir relevo fibroso espesso',
      value: null,
    },
    {
      id: 'diabetes',
      title: 'Diabetes, hemofilia, epilepsia ou coagulopatias',
      subtitle: 'Condições metabólicas de hemostasia e imunidade dérmica',
      value: null,
    },
    {
      id: 'gestante',
      title: 'Gestante ou em período de lactação (amamentação)',
      subtitle: 'Contraindicação médica temporária para tatuagem',
      value: null,
    },
    {
      id: 'alimentacao',
      title: 'Alimentação sólida adequada nas últimas 3 horas',
      subtitle: 'Glicemia suficiente para prevenção de tontura / hipotensão',
      value: null,
    },
    {
      id: 'infeccao',
      title: 'Hepatites, afecções cutâneas ativas ou imunossupressão',
      subtitle: 'Integridade dermatológica da área a ser pigmentada',
      value: null,
    },
  ];

  if (!props.questions || props.questions.length === 0) {
    return defaults;
  }

  // Mescla respostas passadas por props com perguntas padrão
  return defaults.map((def) => {
    const found = props.questions.find((q) => q.id === def.id || q.title.toLowerCase().includes(def.id));
    if (found) {
      return {
        ...def,
        value: found.value,
        detailText: found.detailText,
      };
    }
    return def;
  });
});

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
