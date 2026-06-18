# Monolith Finance - Project Brief & PRD

## 1. Project Overview
Monolith Finance is a minimalist mobile expense tracking application inspired by the clean, high-contrast interfaces of modern Cambodian banking apps. The platform prioritizes speed of entry, clarity of financial data, and a premium monochromatic aesthetic.

## 2. Target Audience
Users looking for a focused, distraction-free tool to manage personal finances, track daily spending, and visualize financial habits without the complexity of traditional banking suites.

## 3. Design System: "Monolith"
*   **Palette**: Strict monochromatic theme (White #FFFFFF, Gray/Surface #F9F9F9, Black #000000).
*   **Typography**: Inter (Sans-serif) for high legibility.
*   **Visual Language**: High contrast, bold weights for primary balances, and subtle gray containers for structure.
*   **Navigation Rule**: The Dashboard (Home) acts as the root; no back button is permitted on the home screen.

## 4. Core Features & Screen Flows

### 4.1 Dashboard (Home)
*   **Purpose**: High-level financial overview.
*   **Key Elements**: Total balance card (Black), Monthly Income/Spent summary, Category quick-scroll, and Recent Transactions.
*   **Navigation**: Primary entry point; no back button.

### 4.2 Add Expense
*   **Purpose**: Rapid transaction entry.
*   **Key Elements**: Large numeric input for amount, grid-based category selection (Dining, Shopping, Travel, etc.), date picker, and optional notes.
*   **Interaction**: Direct "Save Expense" action button.

### 4.3 Insights & Reporting
*   **Purpose**: Visual spending analysis.
*   **Key Elements**: Period toggles (Weekly, Monthly, Yearly), top spending category cards, trend charts (monochromatic), and a detailed percentage breakdown by category.
*   **Export**: Support for generating and downloading PDF financial reports.

### 4.4 Transaction History & Search
*   **Purpose**: Auditing and finding past records.
*   **Key Elements**: Chronological list with merchant icons, date headers, and a dedicated Search screen.
*   **Refinement**: Header simplified to remove unnecessary hamburger menus, focusing on search input and filtering.

## 5. Technical Requirements
*   **Platform**: Mobile (iOS/Android optimized).
*   **Format**: Responsive HTML/CSS.
*   **Components**: Shared navigation (Bottom NavBar) and consistent headers across all secondary screens.

## 6. Future Roadmap
*   Budget goal setting.
*   Currency conversion (USD/KHR).
*   Account-to-account transfers (simulated).
*   Dark Mode optimization.
