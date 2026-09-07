import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { Card } from '../../components/Card';
import { colors } from '../../theme/colors';
import { typography } from '../../theme/typography';
import { spacing } from '../../theme/spacing';

export default function RelatorioMensalScreen() {
  return (
    <View style={styles.container}>
      <Text style={styles.header}>Relatório Mensal</Text>
      <Card style={styles.kpiCard}>
        <Text style={styles.kpiTitle}>Faturamento do Mês</Text>
        <Text style={styles.kpiValue}>R$ 14.850,00</Text>
      </Card>
      <Card style={styles.kpiCard}>
        <Text style={styles.kpiTitle}>Sessões Realizadas</Text>
        <Text style={styles.kpiValue}>32 tatuagens</Text>
      </Card>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background, padding: spacing.md },
  header: { fontSize: typography.sizes.xl, fontWeight: typography.weights.bold, color: colors.text, marginBottom: spacing.md },
  kpiCard: { padding: spacing.lg, alignItems: 'center' },
  kpiTitle: { fontSize: typography.sizes.sm, color: colors.textSecondary, marginBottom: spacing.xs },
  kpiValue: { fontSize: typography.sizes.heading, fontWeight: typography.weights.bold, color: colors.secondary },
});
