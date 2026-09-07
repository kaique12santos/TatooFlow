import React, { useState } from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { Input } from '../../components/Input';
import { Button } from '../../components/Button';
import { colors } from '../../theme/colors';
import { typography } from '../../theme/typography';
import { spacing } from '../../theme/spacing';

export default function ConviteScreen({ navigation }: any) {
  const [codigo, setCodigo] = useState('');

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Ativar Convite</Text>
      <Text style={styles.subtitle}>Digite o código de convite enviado pelo administrador</Text>

      <Input label="Código do Convite" value={codigo} onChangeText={setCodigo} autoCapitalize="characters" />

      <Button title="Validar Convite" onPress={() => navigation.goBack()} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background, padding: spacing.lg, justifyContent: 'center' },
  title: { fontSize: typography.sizes.xxl, fontWeight: typography.weights.bold, color: colors.text, marginBottom: spacing.xs },
  subtitle: { fontSize: typography.sizes.sm, color: colors.textSecondary, marginBottom: spacing.xl },
});
