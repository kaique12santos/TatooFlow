import React, { useState } from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { Input } from '../../components/Input';
import { Button } from '../../components/Button';
import { useAuth } from '../../hooks/useAuth';
import { colors } from '../../theme/colors';
import { typography } from '../../theme/typography';
import { spacing } from '../../theme/spacing';

export default function LoginScreen({ navigation }: any) {
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [loading, setLoading] = useState(false);
  const { signIn } = useAuth();

  const handleLogin = async () => {
    setLoading(true);
    try {
      await signIn(email, senha);
    } finally {
      setLoading(false);
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>TattooFlow</Text>
      <Text style={styles.subtitle}>Gestão de Estúdio & Tatuadores</Text>

      <Input label="E-mail" value={email} onChangeText={setEmail} keyboardType="email-address" autoCapitalize="none" />
      <Input label="Senha" value={senha} onChangeText={setSenha} secureTextEntry />

      <Button title="Entrar" onPress={handleLogin} loading={loading} />

      <Text style={styles.conviteLink} onPress={() => navigation.navigate('Convite')}>
        Possui um código de convite? Clique aqui
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background, padding: spacing.lg, justifyContent: 'center' },
  title: { fontSize: typography.sizes.heading, fontWeight: typography.weights.bold, color: colors.primary, textAlign: 'center' },
  subtitle: { fontSize: typography.sizes.md, color: colors.textSecondary, textAlign: 'center', marginBottom: spacing.xl },
  conviteLink: { color: colors.primary, textAlign: 'center', marginTop: spacing.lg, fontSize: typography.sizes.sm },
});
