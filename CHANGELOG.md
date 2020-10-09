# Changelog

## [1.0.3]

- Added `isChoiceMandatory` to force user submit a non-empty selection list
- Changed _RecyclerPickerDialogTheme_ parent from `Theme.MaterialComponents.Dialog` to `Theme.MaterialComponents.Light.Dialog`
- Changed `onDismiss` to `onItemsPicked`
- Moved `selectableItemBackground` from `RecyclerPickerDialog.Style.Row` to `RecyclerPickerDialog.Style.Row.Choice`
- Gradle plugin updated to 4.0.2

---

## [1.0.2]

- Fixes bug by replacing `btnOk.performContextClick` with `btnOk.performClick`

---

## [1.0.1]

- Added `dismissOnSelection` to dismiss dialog upon choice picked (__note__ that this will make `SelectionType.MULTIPLE` work has `SelectionType.SINGLE`)
- Fixes bug by replacing dismiss with dismissAllowingStateLoss() and checking if isAdded

---

## [1.0.0]

Hello world!