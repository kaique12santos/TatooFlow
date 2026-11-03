<template>
  <div class="bg-[#09090b] border border-[rgba(255,255,255,0.04)] rounded-[2px] p-3 space-y-2">
    <div class="flex items-center gap-2">
      <CheckCircle2 :class="['w-3.5 h-3.5 shrink-0 transition-colors', isLengthValid ? 'text-[#e6c383]' : 'text-[#a39e93]']" />
      <span :class="['font-montserrat text-[11px] transition-colors', isLengthValid ? 'text-[#f2ebd9]' : 'text-[#a39e93]']">
        6 dígitos numéricos obrigatórios
      </span>
    </div>

    <div class="flex items-center gap-2">
      <CheckCircle2 :class="['w-3.5 h-3.5 shrink-0 transition-colors', isNonTrivial ? 'text-[#e6c383]' : 'text-[#a39e93]']" />
      <span :class="['font-montserrat text-[11px] transition-colors', isNonTrivial ? 'text-[#f2ebd9]' : 'text-[#a39e93]']">
        Sem sequências triviais (ex: 123456, 111111)
      </span>
    </div>

    <div class="flex items-center gap-2 opacity-80">
      <Circle :class="['w-3.5 h-3.5 shrink-0 transition-colors', pinLength > 0 ? 'text-[#e6c383]' : 'text-[#a39e93]']" />
      <span class="font-montserrat text-[11px] text-[#a39e93]">
        Evite datas de nascimento ou dados públicos
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { CheckCircle2, Circle } from '@lucide/vue';

const props = defineProps<{
  pin: string;
}>();

const pinLength = computed(() => props.pin.length);
const isLengthValid = computed(() => props.pin.length === 6);

const isNonTrivial = computed(() => {
  if (props.pin.length < 6) return false;
  const trivialPatterns = ['123456', '654321', '000000', '111111', '222222', '333333', '444444', '555555', '666666', '777777', '888888', '999999'];
  return !trivialPatterns.includes(props.pin);
});
</script>
