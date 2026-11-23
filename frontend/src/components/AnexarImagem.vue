<template>
  <div
    :class="[
      'overflow-hidden flex items-center justify-center shrink-0 select-none transition-all',
      shape === 'circle' ? 'rounded-full' : 'rounded-[2px]',
      customClass || 'bg-[#09090b] border border-[rgba(201,168,106,0.3)]'
    ]"
    :style="{
      width: typeof size === 'number' ? `${size}px` : size,
      height: typeof size === 'number' ? `${size}px` : size
    }"
  >
    <img
      v-if="currentSrc && !hasError"
      :src="currentSrc"
      :alt="alt || name || 'Avatar'"
      class="w-full h-full object-cover"
      @error="handleError"
    />
    <span
      v-else-if="initials"
      class="font-playfair font-bold text-[#c9a86a] flex items-center justify-center leading-none"
      :style="{ fontSize: initialFontSize }"
    >
      {{ initials }}
    </span>
    <User v-else class="text-[#c9a86a]" :style="{ width: iconSize, height: iconSize }" />
  </div>
</template>

<script lang="ts">
/**
 * Array com 18 URLs públicas fictícias de perfis da internet (Unsplash CDN).
 * Caso algum link esteja inválido ou queira alterar, edite os itens abaixo.
 */
export const MOCK_AVATARS: string[] = [
  'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=300&q=80',
  'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=300&q=80',
  'https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=300&q=80',
  'https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&w=300&q=80',
  'https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=300&q=80',
  'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=300&q=80',
  'https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=300&q=80',
  'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=300&q=80',
  'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=300&q=80',
  'https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?auto=format&fit=crop&w=300&q=80',
  'https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?auto=format&fit=crop&w=300&q=80',
  'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=300&q=80',
  'https://images.unsplash.com/photo-1560250097-0b93528c311a?auto=format&fit=crop&w=300&q=80',
  'https://images.unsplash.com/photo-1580489944761-15a19d654956?auto=format&fit=crop&w=300&q=80',
  'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=300&q=80',
  'https://images.unsplash.com/photo-1548142813-c348350df52b?auto=format&fit=crop&w=300&q=80',
  'https://images.unsplash.com/photo-1527980965255-d3b416303d12?auto=format&fit=crop&w=300&q=80',
  'https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?auto=format&fit=crop&w=300&q=80',
];

/**
 * Retorna uma imagem puramente aleatória da lista de avatares.
 */
export function getRandomAvatar(): string {
  const index = Math.floor(Math.random() * MOCK_AVATARS.length);
  return MOCK_AVATARS[index]!;
}

/**
 * Retorna uma imagem mockada para o cliente ou usuário.
 * Se informado seed (ex: id, nome, email), faz o hash determinístico para que o mesmo
 * usuário mantenha a mesma foto consistente na tela sem piscar ao re-renderizar.
 * Caso seed seja nulo/vazio, sorteia via random.
 */
export function getMockAvatar(seed?: string | number | null): string {
  if (seed !== undefined && seed !== null && seed !== '') {
    const str = String(seed);
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
      hash = (hash << 5) - hash + str.charCodeAt(i);
      hash |= 0;
    }
    const index = Math.abs(hash) % MOCK_AVATARS.length;
    return MOCK_AVATARS[index]!;
  }
  return getRandomAvatar();
}
</script>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { User } from '@lucide/vue';

const props = withDefaults(
  defineProps<{
    src?: string | null;
    seed?: string | number | null;
    name?: string;
    alt?: string;
    size?: number | string;
    shape?: 'circle' | 'square';
    customClass?: string;
  }>(),
  {
    src: '',
    seed: null,
    name: '',
    alt: '',
    size: 40,
    shape: 'square',
    customClass: '',
  }
);

const hasError = ref(false);

const currentSrc = computed(() => {
  if (props.src) return props.src;
  if (props.seed !== null && props.seed !== undefined && props.seed !== '') {
    return getMockAvatar(props.seed);
  }
  if (props.name) {
    return getMockAvatar(props.name);
  }
  return '';
});

watch(
  () => [props.src, props.seed, props.name],
  () => {
    hasError.value = false;
  }
);

function handleError() {
  hasError.value = true;
}

const initials = computed(() => {
  if (!props.name) return '';
  const parts = props.name.trim().split(/\s+/);
  if (parts.length === 1) return parts[0]!.slice(0, 2).toUpperCase();
  return (parts[0]![0]! + parts[parts.length - 1]![0]!).toUpperCase();
});

const initialFontSize = computed(() => {
  const numSize = typeof props.size === 'number' ? props.size : parseInt(String(props.size), 10) || 40;
  return `${Math.round(numSize * 0.42)}px`;
});

const iconSize = computed(() => {
  const numSize = typeof props.size === 'number' ? props.size : parseInt(String(props.size), 10) || 40;
  return `${Math.round(numSize * 0.5)}px`;
});
</script>
