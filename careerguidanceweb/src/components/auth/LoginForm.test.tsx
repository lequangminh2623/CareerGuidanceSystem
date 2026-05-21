import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import LoginForm from './LoginForm';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import authReducer from '@/store/features/auth/authSlice';

// 1. Mocking Next.js Navigation
jest.mock('next/navigation', () => ({
  useSearchParams: () => new URLSearchParams(),
}));

// 2. Mocking react-i18next
jest.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => key,
  }),
}));

// 3. Mocking cookies-next
jest.mock('cookies-next', () => ({
  setCookie: jest.fn(),
}));

// 4. Mocking API / Axios (Optional for basic render, needed for submit test)
jest.mock('@/lib/utils/api', () => {
  return {
    __esModule: true,
    default: {
      post: jest.fn(),
    },
    authApis: jest.fn(() => ({
      get: jest.fn(),
    })),
    endpoints: {
      login: '/api/login',
      profile: '/api/profile',
    },
  };
});

// Mock Google Login Component to avoid dealing with Google Auth Provider in tests
jest.mock('@/components/GoogleLogin/GoogleLoginButton', () => {
  return function MockGoogleLoginButton() {
    return <button data-testid="google-login-btn">Google Login Mock</button>;
  };
});

// Cấu hình mock store
const mockStore = configureStore({
  reducer: {
    auth: authReducer,
  },
});

const renderWithProviders = (ui: React.ReactElement) => {
  return render(
    <Provider store={mockStore}>
      {ui}
    </Provider>
  );
};

describe('LoginForm Component', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render the login form correctly', () => {
    renderWithProviders(<LoginForm />);
    
    // Check headings/texts based on translation keys
    expect(screen.getByText('login')).toBeInTheDocument();
    
    // Check input fields exist
    expect(screen.getByPlaceholderText('email-placeholder')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('password-placeholder')).toBeInTheDocument();
    
    // Check submit button exists
    expect(screen.getByRole('button', { name: 'login-now' })).toBeInTheDocument();
  });

  it('should display validation errors when submitting empty form', async () => {
    renderWithProviders(<LoginForm />);
    
    const submitBtn = screen.getByRole('button', { name: 'login-now' });
    
    // Trigger submit without filling inputs
    fireEvent.click(submitBtn);
    
    // Validate error messages show up
    await waitFor(() => {
      expect(screen.getByText('email-empty')).toBeInTheDocument();
      expect(screen.getByText('password-empty')).toBeInTheDocument();
    });
  });

  it('should validate email format correctly', async () => {
    renderWithProviders(<LoginForm />);
    
    const emailInput = screen.getByPlaceholderText('email-placeholder');
    const submitBtn = screen.getByRole('button', { name: 'login-now' });

    // Type invalid email (doesn't match @ou.edu.vn)
    fireEvent.change(emailInput, { target: { value: 'test@gmail.com' } });
    fireEvent.click(submitBtn);

    await waitFor(() => {
      expect(screen.getByText('email-invalid')).toBeInTheDocument();
    });
  });
});
