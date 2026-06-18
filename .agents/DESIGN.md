---
name: Monolith Finance
colors:
  surface: '#f9f9f9'
  surface-dim: '#dadada'
  surface-bright: '#f9f9f9'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f3f3f3'
  surface-container: '#eeeeee'
  surface-container-high: '#e8e8e8'
  surface-container-highest: '#e2e2e2'
  on-surface: '#1a1c1c'
  on-surface-variant: '#4c4546'
  inverse-surface: '#2f3131'
  inverse-on-surface: '#f1f1f1'
  outline: '#7e7576'
  outline-variant: '#cfc4c5'
  surface-tint: '#5e5e5e'
  primary: '#000000'
  on-primary: '#ffffff'
  primary-container: '#1b1b1b'
  on-primary-container: '#848484'
  inverse-primary: '#c6c6c6'
  secondary: '#5e5e5e'
  on-secondary: '#ffffff'
  secondary-container: '#e3e2e2'
  on-secondary-container: '#646464'
  tertiary: '#000000'
  on-tertiary: '#ffffff'
  tertiary-container: '#1b1b1b'
  on-tertiary-container: '#848484'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#e2e2e2'
  primary-fixed-dim: '#c6c6c6'
  on-primary-fixed: '#1b1b1b'
  on-primary-fixed-variant: '#474747'
  secondary-fixed: '#e3e2e2'
  secondary-fixed-dim: '#c7c6c6'
  on-secondary-fixed: '#1b1c1c'
  on-secondary-fixed-variant: '#464747'
  tertiary-fixed: '#e2e2e2'
  tertiary-fixed-dim: '#c6c6c6'
  on-tertiary-fixed: '#1b1b1b'
  on-tertiary-fixed-variant: '#474747'
  background: '#f9f9f9'
  on-background: '#1a1c1c'
  surface-variant: '#e2e2e2'
typography:
  display-lg:
    fontFamily: Inter
    fontSize: 48px
    fontWeight: '700'
    lineHeight: 56px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
    letterSpacing: -0.01em
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  body-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  label-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
    letterSpacing: 0.01em
  label-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.05em
  numeric-xl:
    fontFamily: Inter
    fontSize: 40px
    fontWeight: '700'
    lineHeight: 48px
    letterSpacing: -0.03em
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  base: 4px
  xs: 4px
  sm: 8px
  md: 16px
  lg: 24px
  xl: 40px
  container-margin: 20px
  gutter: 16px
---

## Brand & Style

The brand identity centers on absolute clarity, precision, and trust. By removing the distraction of color, the design system forces focus onto data and utility, evoking a sense of institutional stability and premium exclusivity. 

The design style is a blend of **High-Contrast Minimalism** and **Swiss-inspired Modernism**. It utilizes a strict monochromatic palette to create a high-end, "archival" feel similar to luxury financial reporting or premium editorial layouts. The emotional response should be one of calm control—where the interface disappears to let the user’s financial health take center stage. 

Whitespace is treated as a functional element, used generously to separate disparate financial concepts without the need for heavy containers or lines.

## Colors

The palette is strictly restricted to a grayscale spectrum to ensure maximum legibility and a sophisticated aesthetic.

*   **Primary (#000000):** Reserved for the highest level of hierarchy, including primary headings, active icons, and main call-to-action buttons.
*   **Secondary (#757575):** Used for supporting information, captions, and inactive states to create clear visual distance from primary data.
*   **Surface / Border (#E0E0E0 & #F5F5F5):** Used for subtle structural separation. #F5F5F5 acts as a background for grouped elements (like cards or list items), while #E0E0E0 is used for thin, 1px dividers.
*   **Background (#FFFFFF):** The canvas of the application, ensuring a "paper-white" clean look.

## Typography

This design system utilizes **Inter** across all levels to maintain a systematic, utilitarian appearance. 

*   **Financial Data:** For currency amounts and balances, use `numeric-xl`. The tight letter-spacing mimics the look of high-end ticker displays.
*   **Hierarchy:** Use bold weights (600-700) for headers to contrast against the stark white background. 
*   **Micro-copy:** Use `label-sm` with uppercase transformation for category headers or section titles to provide a structured, architectural feel.

## Layout & Spacing

The layout follows a strict **4px baseline grid** to ensure mathematical precision in element alignment.

*   **Grid Model:** A 12-column fluid grid for desktop and a 4-column grid for mobile. 
*   **Margins:** Maintain a minimum 20px "safe zone" on the left and right edges of mobile devices.
*   **Rhythm:** Vertical spacing between unrelated sections should use `xl` (40px). Related items within a group should use `md` (16px).
*   **Data Density:** Since this is a finance application, allow for higher density in list views by using `sm` (8px) padding between rows, provided the text contrast remains high.

## Elevation & Depth

To maintain the minimalist aesthetic, this design system avoids traditional drop shadows. Depth is achieved through **Tonal Layering** and **Low-Contrast Outlines**.

*   **Layering:** Elements are stacked using color rather than shadow. Level 0 is the white background (#FFFFFF). Level 1 elements (like cards or search bars) use the light gray surface (#F5F5F5).
*   **Outlines:** Interactive components should use a 1px solid border (#E0E0E0). For active or focused states, the border weight remains 1px but the color shifts to black (#000000).
*   **Zero Shadows:** Do not use blurs or ambient shadows. Separation must be defined by edge-to-edge color changes or distinct 1px lines.

## Shapes

The shape language is sharp and disciplined. 

*   **Primary Radius:** A consistent 4px radius is applied to buttons, input fields, and containers. This "softened sharp" look retains the professional rigor of a 0px corner but feels modern and refined.
*   **Icons:** Use "stroke" style icons with a 1.5pt or 2pt weight. Avoid filled icons unless they represent a selected state. Icons should be strictly geometric.

## Components

*   **Buttons:** 
    *   *Primary:* Solid black (#000000) background with white text. 4px radius.
    *   *Secondary:* White background with a 1px border (#E0E0E0) and black text.
*   **Input Fields:** Ghost style. No background color, only a bottom border of 1px (#E0E0E0). When focused, the border becomes 1px black (#000000). Labels should be `label-sm` positioned above the input.
*   **Cards:** Use the `neutral` color (#F5F5F5) for the background with no border, or a white background with a 1px border (#E0E0E0). Do not use both simultaneously.
*   **Lists:** List items should be separated by 1px dividers (#E0E0E0). Use `body-md` for the title and `label-md` in secondary gray (#757575) for subtitles.
*   **Chips/Tags:** Small 4px rounded rectangles with #F5F5F5 background and #757575 text. Used for transaction categories or status labels (e.g., "Pending").
*   **Keypad:** For PIN entry or currency input, use large, clean digits with no circular background containers; use only the whitespace to define the grid.