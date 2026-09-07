import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { Button } from '../../components/Button';
import { colors } from '../../theme/colors';
import { typography } from '../../theme/typography';
import { spacing } from '../../theme/spacing';

export default function UploadMidiaScreen() {
  return (
    <View style={styles.container}>
      <Text style={styles.header}>Upload de Mídias da Sessão</Text>
      <Text style={styles.subtitle}>Registre fotos do resultado final e detalhes da tatuagem</Text>
      <Button title="Selecionar Foto da Galeria" onPress={() => {}} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background, padding: spacing.md, justifyContent: 'center' },
  header: { fontSize: typography.sizes.xl, fontWeight: typography.weights.bold, color: colors.text, marginBottom: spacing.xs },
  subtitle: { fontSize: typography.sizes.sm, color: colors.textSecondary, marginBottom: spacing.xl },
});
