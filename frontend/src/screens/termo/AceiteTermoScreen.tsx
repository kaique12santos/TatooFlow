import React from 'react';
import { View, Text, StyleSheet, ScrollView } from 'react-native';
import { Button } from '../../components/Button';
import { colors } from '../../theme/colors';
import { typography } from '../../theme/typography';
import { spacing } from '../../theme/spacing';

export default function AceiteTermoScreen() {
  return (
    <View style={styles.container}>
      <Text style={styles.header}>Termo de Consentimento e Responsabilidade</Text>
      <ScrollView style={styles.termScroll}>
        <Text style={styles.termText}>
          Declaro que fui informado(a) sobre todos os cuidados pré e pós-tatuagem, riscos de alergia e cicatrização.
          Autorizo a realização do procedimento pelo tatuador responsável.
        </Text>
      </ScrollView>
      <Button title="Coletar Assinatura Digital" onPress={() => {}} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background, padding: spacing.md },
  header: { fontSize: typography.sizes.lg, fontWeight: typography.weights.bold, color: colors.text, marginBottom: spacing.md },
  termScroll: { flex: 1, backgroundColor: colors.surface, padding: spacing.md, borderRadius: 8, marginBottom: spacing.md },
  termText: { color: colors.textSecondary, fontSize: typography.sizes.sm, lineHeight: 22 },
});
