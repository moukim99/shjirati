# Shjirati Offline Plant Catalog

The catalog is intentionally separate from the user's Room plants table.

- `plant_catalog.json`: bundled plant knowledge.
- Room plants: plants created by the user.

The catalog now contains **200 curated plants**. Arabic is the primary language.

- **TREE** entries use `HARVEST`.
- **VEGETABLE** and **HERB** entries use `GERMINATION`.
- The current practical categories are UX-oriented; they are not intended to be a strict botanical taxonomy.

Each entry reserves one compressed WebP image path. Image files and attribution/license metadata are deliberately not fabricated; they will be added only after source verification.

Next steps:
1. Verify source and license for every image.
2. Add real WebP images.
3. Add verified growing data.
4. Connect catalog selection to the add-plant flow.
5. Keep automated catalog validation tests in sync with the dataset.
6. Review category semantics before introducing additional plant types.
