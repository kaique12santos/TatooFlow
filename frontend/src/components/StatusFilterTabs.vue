<template>
  <div ref="scrollContainer" class="status-filter-scroll mb-5" :class="{ 'is-dragging': isDragging }"
    role="group" aria-label="Filtros por status" tabindex="0" @wheel="scrollWithWheel"
    @pointerdown="startDrag" @pointermove="dragFilters" @pointerup="endDrag" @pointercancel="endDrag"
    @lostpointercapture="endDrag" @click.capture="cancelDragClick" @dragstart.prevent>
    <div class="flex flex-nowrap items-center gap-1.5 w-max pb-2">
      <button
        v-for="tab in tabs"
        :key="tab.id"
        @click="selectFilter(tab.id)"
        :aria-pressed="modelValue === tab.id"
        :class="[
          'flex shrink-0 items-center gap-1.5 whitespace-nowrap px-3 py-1.5 rounded-[2px] font-inter text-[11px] font-semibold transition-all duration-200 cursor-pointer',
          modelValue === tab.id
            ? 'bg-[#c9a86a] text-[#412d00] shadow-md font-bold'
            : 'bg-[#141416] text-[#f2ebd9] hover:bg-[#1a1a1e] border border-[rgba(255,255,255,0.03)]'
        ]"
      >
        <span v-if="tab.dotColor && modelValue !== tab.id" :class="['w-2 h-2 rounded-[1px]', tab.dotColor]"></span>
        <span>{{ tab.label }}</span>
        <span
          :class="[
            'px-1.5 py-0.2 font-jetbrains text-[9px] rounded-[2px]',
            modelValue === tab.id
              ? 'bg-[#09090b] text-[#e6c383]'
              : 'bg-[rgba(255,255,255,0.05)] text-[#a39e93]'
          ]"
        >
          {{ tab.count }}
        </span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';

export interface FilterTab {
  id: string;
  label: string;
  count: number;
  dotColor?: string;
}

const props = defineProps<{
  modelValue: string;
  tabs: FilterTab[];
}>();

const scrollContainer = ref<HTMLElement | null>(null);
const isDragging = ref(false);
let dragPointer: number | null = null;
let dragStartX = 0;
let dragStartScroll = 0;
let suppressClick = false;
onMounted(revealSelectedFilter);
watch(() => props.modelValue, revealSelectedFilter, { flush: 'post' });

function revealSelectedFilter() {
  const container = scrollContainer.value;
  const selected = container?.querySelector<HTMLElement>('[aria-pressed="true"]');
  if (!container || !selected) return;
  const viewport = container.getBoundingClientRect();
  const button = selected.getBoundingClientRect();
  const left = button.left < viewport.left ? button.left - viewport.left
    : button.right > viewport.right ? button.right - viewport.right : 0;
  if (left) container.scrollBy({ left, behavior: 'smooth' });
}

function scrollWithWheel(event: WheelEvent) {
  const container = scrollContainer.value;
  if (!container || event.ctrlKey || Math.abs(event.deltaX) >= Math.abs(event.deltaY)) return;
  const previous = container.scrollLeft;
  const step = event.deltaMode === 1 ? 16 : event.deltaMode === 2 ? container.clientWidth : 1;
  container.scrollLeft += event.deltaY * step;
  if (container.scrollLeft !== previous) event.preventDefault();
}

function startDrag(event: PointerEvent) {
  suppressClick = false;
  const container = scrollContainer.value;
  if (event.pointerType !== 'mouse' || event.button !== 0 || !container || container.scrollWidth <= container.clientWidth) return;
  dragPointer = event.pointerId;
  dragStartX = event.clientX;
  dragStartScroll = container.scrollLeft;
}

function dragFilters(event: PointerEvent) {
  const container = scrollContainer.value;
  if (!container || event.pointerId !== dragPointer) return;
  const distance = event.clientX - dragStartX;
  if (!isDragging.value && Math.abs(distance) < 6) return;
  if (!isDragging.value) {
    isDragging.value = true;
    suppressClick = true;
    container.setPointerCapture(event.pointerId);
  }
  event.preventDefault();
  container.scrollLeft = dragStartScroll - distance;
}

function endDrag(event: PointerEvent) {
  if (event.pointerId !== dragPointer) return;
  dragPointer = null;
  isDragging.value = false;
  if (scrollContainer.value?.hasPointerCapture(event.pointerId)) scrollContainer.value.releasePointerCapture(event.pointerId);
}

function cancelDragClick(event: MouseEvent) {
  if (!suppressClick || event.detail === 0) return;
  event.preventDefault();
  event.stopPropagation();
  suppressClick = false;
}

const emit = defineEmits<{
  'select-filter': [id: string];
  'update:modelValue': [id: string];
}>();

function selectFilter(id: string) {
  emit('update:modelValue', id);
  emit('select-filter', id);
}
</script>

<style scoped>
.status-filter-scroll {
  width: 100%;
  max-width: 100%;
  min-width: 0;
  overflow-x: auto;
  overflow-y: hidden;
  overscroll-behavior-x: contain;
  scrollbar-width: none;
  -webkit-overflow-scrolling: touch;
  cursor: grab;
  user-select: none;
}
.status-filter-scroll::-webkit-scrollbar { display: none; }
.status-filter-scroll.is-dragging,
.status-filter-scroll.is-dragging button { cursor: grabbing; }
button { min-height: 32px; padding: 6px 12px; }
</style>
