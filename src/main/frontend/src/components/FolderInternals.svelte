<script>
	import {mdiDelete, mdiLabel, mdiLabelOff, mdiPlaylistPlay} from '@mdi/js'
	import Clickable from "./Clickable.svelte";
	import Tooltip from "./Tooltip.svelte";
	import { getFileString } from "../utils";
	import { Badge, Icon } from 'svelte-materialify/src';

	export let file;
	export let expandable = false;
	export let expanded = false;
	export let detailed = false;
</script>

<Tooltip tooltipText={file.type}>
	<Clickable {file} bind:expanded>
		{#if expandable}
			{#if expanded}▾{:else}▸{/if}
		{/if}

		{#if file.status == "Draft"}
			<Icon style="color: inherit" path={mdiPlaylistPlay} />
		{:else if file.status == "Published"}
			<Icon style="color: inherit" path={mdiLabel} />
		{:else if file.status == "Deprecated"}
			<Icon style="color: inherit" path={mdiLabelOff} />
		{:else if file.status == "Removed"}
			<Icon style="color: inherit" path={mdiDelete} />
		{/if}
		{getFileString(file, detailed)}
		<!-- <Badge class="{status.color}-color" value={status.text}/> -->
		{#if file.scope && file.category}
			<Badge class="primary-color" value={file.scope + " " + file.category} offsetX={2} offsetY={1}/>
		{:else if file.scope}
			<Badge class="primary-color" value={file.scope} offsetX={2} offsetY={1}/>
		{:else if file.category}
			<Badge class="primary-color" value={file.category} offsetX={2} offsetY={1}/>
		{/if}
	</Clickable>
</Tooltip>