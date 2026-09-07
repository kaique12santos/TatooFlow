import React, { createContext, useState, ReactNode } from 'react';
import { Usuario } from '../types';

interface AuthContextData {
  user: Usuario | null;
  signed: boolean;
  signIn: (email: string, pass: string) => Promise<void>;
  signOut: () => void;
}

export const AuthContext = createContext<AuthContextData>({} as AuthContextData);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<Usuario | null>(null);

  const signIn = async (email: string, pass: string) => {
    // Mock login
    setUser({ id: 1, nome: 'Tatuador Demo', email, perfil: 'TATUADOR', ativo: true });
  };

  const signOut = () => {
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, signed: !!user, signIn, signOut }}>
      {children}
    </AuthContext.Provider>
  );
};
