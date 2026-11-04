<template>
  <ion-modal :is-open="isOpen" :aria-labelledby="labelledBy" class="compact-modal" @didDismiss="$emit('close')">
    <div class="max-h-[calc(100dvh-48px)] overflow-y-auto bg-[#141416] p-4 text-[#f2ebd9]">
      <slot />
    </div>
  </ion-modal>
</template>

<script setup lang="ts">
import { IonModal } from '@ionic/vue';

defineProps<{ isOpen: boolean; labelledBy: string }>();
defineEmits<{ close: [] }>();
</script>

<style scoped>
.compact-modal {
  --width: calc(100% - 32px);
  --max-width: 384px;
  --height: auto;
  --border-radius: 4px;
  --border-width: 1px;
  --border-style: solid;
  --border-color: rgba(201, 168, 106, 0.4);
  --background: #141416;
  --backdrop-opacity: 0.8;
  --box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.65);
}
.compact-modal::part(content) { box-sizing: border-box; }
.compact-modal::part(backdrop) { backdrop-filter: blur(12px); }
</style>
