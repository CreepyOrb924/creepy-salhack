# Inventory parity expansion

`InventoryModules.java` now ports original AutoTotem and AutoArmor settings from the pinned combat sources. Existing AutoTool and ChestStealer code is preserved.

## AutoTotem

Implemented Health (16,0..20,.5), Mode (Totem/Gap/Crystal/Pearl/Chorus/Strength/Shield), Fallback (same choices, default Crystal), FallDistance (15,0..100,10), TotemOnElytra=true, SwordGap=false, StrGap=false and HotbarFirst=false. Removed the old partial port's Delay setting: upstream has no such setting and evaluates each player update.

Priority matches upstream: above/equal health threshold, a sword's missing-strength override precedes SwordGap, even before flight/fall gates. Otherwise low health, Totem mode, configured elytra flying, excess non-elytra fall distance or Crystal mode with no non-friend player within15 blocks select a totem. Desired item, configured fallback, then emergency totem are searched with the original fallback gating. Main inventory, hotbar and crafting inputs are usable sources; output/armor slots are excluded. HotbarFirst searches hotbar right-to-left before main inventory, like the original reverse screen-slot scan.

Intentional corrections: Strength recognizes drinkable potions that actually grant Strength (upstream accepts every drinkable potion); Gap accepts both modern golden-apple item IDs, corresponding to the old item's variants. A single ordinary SWAP with offhand button40 replaces the old three PICKUP transactions, preserving the empty cursor. Inventory operations pause for screens, active cursors or other containers; upstream permits its ClickGUI/player inventory screen. No per-swap chat spam or current-mode ArrayList metadata is added because the shared module API has no metadata interface.

## AutoArmor

Implemented Delay in milliseconds (50,0..1000,1), Curse=false, Elytra=false and ElytraReplace=false. Like the original, fills **empty** armor slots and ranks matching armor by protection points plus Protection enchantment level. Existing occupied armor is not automatically upgraded. Optional Elytra prefers usable wings above3% durability for an empty chest slot. ElytraReplace exchanges unusable equipped wings for usable spare wings. Cursor-empty/player-handler guards apply to ordinary click swaps, and the previous wings return to the selected spare's source slot. No item is thrown or discarded, and full main inventories still allow an exact source/target exchange.

AutoArmor pauses while AutoMendArmor/AutoMend owns the armor. Crafting-input and offhand armor are supported, including XCarry's stored crafting inputs. Curse checks Binding, matching upstream rather than banning Vanishing too. Broken-wing replacement also respects the millisecond delay and Binding removal restrictions; upstream's replacement branch bypasses its timer. Elytra preference does not replace a worn chestplate, matching upstream. Each tick performs at most one equipment exchange, even with Delay0; upstream can fill several empty slots in the same update.

## Verification

`gradle compileJava test --tests '*MovementMathTest' --tests '*InventoryDecisionsTest' --offline` passed (six tests total). Three inventory tests exercise original sword override precedence, health equality/emergency boundaries, Crystal nearby-enemy gating, elytra/fall separation and screen-slot ordering/exclusions. Integrated-server item movement still requires runtime QA; these pure tests prove policy and indexing rather than server transaction acceptance.
