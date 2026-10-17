export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface RegistrationRequest {
  name: string;
  email: string;
  password: string;
  passwordConfirmation: string;
}

export interface RegistrationResponse {
  id: number;
  name: string;
  email: string;
  role: string;
}
