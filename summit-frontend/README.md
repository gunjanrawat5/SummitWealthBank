# Summit Wealth Bank - Frontend

A modern banking application frontend built with React, Vite, and Tailwind CSS.

## Features

- 🔐 **Authentication System** - Secure login/register with JWT token management
- 📊 **Dashboard** - Overview of accounts, balances, and recent transactions
- 💳 **Account Management** - View and open checking/savings accounts
- 💸 **Money Transfer** - Transfer funds between accounts or to external banks
- 📈 **Wealth Management** - Investment portfolio tracking and management
- 📝 **Transaction History** - Detailed transaction tracking with filtering
- 🔔 **Real-time Card Feed** - Live monitoring of card transactions
- 👨‍💼 **Admin Dashboard** - User management and system monitoring (admin only)

## Tech Stack

- **React 18** - Frontend framework
- **Vite** - Build tool and dev server
- **Tailwind CSS** - Utility-first CSS framework
- **React Router** - Client-side routing
- **Axios** - HTTP client
- **Recharts** - Data visualization
- **Lucide React** - Icon library
- **Radix UI** - Headless UI components

## Prerequisites

- Node.js 16+ and npm/yarn installed
- Spring Boot backend running on port 8080

## Installation

1. **Navigate to the frontend directory:**
   ```bash
   cd summit-frontend
   ```

2. **Install dependencies:**
   ```bash
   npm install
   ```

3. **Start the development server:**
   ```bash
   npm run dev
   ```

   The application will open at `http://localhost:3000`

## Project Structure

```
summit-frontend/
├── src/
│   ├── components/
│   │   ├── auth/          # Login and Register components
│   │   ├── layout/        # Layout with sidebar navigation
│   │   └── pages/         # Page components (Dashboard, Accounts, etc.)
│   ├── contexts/
│   │   └── AuthContext.jsx    # Authentication context provider
│   ├── utils/
│   │   └── api.js         # Axios configuration
│   ├── App.jsx            # Main app component with routing
│   ├── main.jsx          # Entry point
│   └── index.css         # Global styles with Tailwind
├── public/               # Static assets
├── index.html           # HTML template
├── package.json         # Dependencies and scripts
├── vite.config.js      # Vite configuration
├── tailwind.config.js  # Tailwind configuration
└── postcss.config.js   # PostCSS configuration
```

## Available Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint

## API Integration

The frontend connects to the Spring Boot backend running on `http://localhost:8080`. API calls are configured in:

- `/src/utils/api.js` - Axios configuration with interceptors
- `/src/contexts/AuthContext.jsx` - Authentication API calls

### API Endpoints Used

- **Auth**: `/api/auth/login`, `/api/auth/register`, `/api/auth/admin/login`
- **Accounts**: `/api/accounts`, `/api/accounts/open`
- **Transfers**: `/api/transfers`, `/api/transfers/recent`
- **Transactions**: `/api/transactions`, `/api/transactions/recent`
- **Wealth**: `/api/wealth/portfolio`, `/api/wealth/invest`
- **Card Feed**: `/api/card-feed`
- **Admin**: `/api/admin/users`, `/api/admin/stats`

## Authentication Flow

1. User logs in via `/login` page
2. JWT token is stored in localStorage
3. Token is attached to all API requests via Axios interceptor
4. Protected routes check authentication status
5. Token expiration redirects to login

## Styling

The app uses Tailwind CSS for styling with custom configuration:

- Custom color palette matching the bank branding
- Responsive design for all screen sizes
- Custom animations and transitions
- Glass morphism effects for modern UI

## Environment Variables

Create a `.env` file in the root directory:

```env
VITE_API_URL=http://localhost:8080
```

## Building for Production

1. **Build the app:**
   ```bash
   npm run build
   ```

2. **The build output will be in the `dist` folder**

3. **Serve using any static server or integrate with Spring Boot**

## Integration with Spring Boot

To serve the React app from Spring Boot:

1. Build the React app: `npm run build`
2. Copy the `dist` folder contents to Spring Boot's `src/main/resources/static`
3. Spring Boot will serve the React app at the root URL

## Demo Mode

The application includes mock data for demonstration when the backend is not available. Each component has fallback mock data to showcase functionality.

## Security Notes

- JWT tokens are stored in localStorage (consider using httpOnly cookies in production)
- All API routes require authentication except login/register
- Admin routes are protected and only accessible to users with ADMIN role
- Sensitive data (like full account numbers) are masked in the UI

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Open a pull request

## License

MIT

## Support

For issues or questions, please contact the development team.
