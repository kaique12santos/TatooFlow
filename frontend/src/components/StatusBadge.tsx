import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { colors } from '../theme/colors';
import { typography } from '../theme/typography';
import { spacing } from '../theme/spacing';

interface StatusBadgeProps {
  status: string;
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({ status }) => {
  const getBadgeColor = () => {
    switch (status.toUpperCase()) {
      case 'CONCLUIDO':
      case 'APROVADO':
      case 'PAGO':
        return colors.secondary;
      case 'PENDENTE':
      case 'EM_ANDAMENTO':
        return colors.warning;
      case 'CANCELADO':
      case 'REJEITADO':
        return colors.danger;
      default:
        return colors.textMuted;
    }
  };

  return (
    <View style={[styles.badge, { backgroundColor: getBadgeColor() + '20', borderColor: getBadgeColor() }]}>
      <Text style={[styles.text, { color: getBadgeColor() }]}>{status}</Text>
    </View>
  );
};

const styles = StyleSheet.create({
  badge: {
    paddingHorizontal: spacing.sm,
    paddingVertical: spacing.xs,
    borderRadius: 16,
    borderWidth: 1,
    alignSelf: 'flex-start',
  },
  text: {
    fontSize: typography.sizes.xs,
    fontWeight: typography.weights.bold,
  },
});
