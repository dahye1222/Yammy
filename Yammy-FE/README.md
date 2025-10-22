# Yammy Frontend

## Tech Stack

| Category | Technology |
|----------|------------|
| Main Library | React.js |
| State Management | Zustand |
| CSS Framework | Tailwind CSS |
| Routing | React Router |
| Package Manager | npm |
| Build Tool | Vite |
| Deployment | AWS EC2 + Nginx |
| Language | JavaScript |
| Architecture | FSD (Feature-Sliced Design) |

## Project Structure (FSD)

```
src/
├── app/                # Application initialization
│   ├── providers/      # App providers (Router, etc.)
│   └── App.jsx        # Root component
├── pages/             # Page components
├── widgets/           # Large composite components
├── features/          # User interactions features
├── entities/          # Business entities
└── shared/            # Shared resources
    ├── ui/           # Shared UI components
    ├── lib/          # Utilities
    ├── api/          # API client
    ├── config/       # Configuration (stores, etc.)
    └── hooks/        # Custom hooks
```

## Getting Started

1. Install dependencies:
```bash
npm install
```

2. Set up environment variables:
```bash
cp .env.example .env
```

3. Run development server:
```bash
npm run dev
```

4. Build for production:
```bash
npm run build
```

## Available Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint

## Environment Variables

Create a `.env` file in the root directory:

```
VITE_API_BASE_URL=http://localhost:8080/api
```
