import { checkPermission, formatVietnamTime, capitalizeFirstWord } from './index';

describe('Utility Functions - utils/index.ts', () => {
  describe('checkPermission', () => {
    it('should return true when objectUserId and userId are identical', () => {
      expect(checkPermission('user123', 'user123')).toBe(true);
      expect(checkPermission(1, 1)).toBe(true);
    });

    it('should return false when objectUserId and userId are different', () => {
      expect(checkPermission('user123', 'user456')).toBe(false);
      expect(checkPermission(1, 2)).toBe(false);
      expect(checkPermission('1', 1)).toBe(false); // strict equality test
    });
  });

  describe('formatVietnamTime', () => {
    it('should format UTC date string to Vietnam time correctly', () => {
      const utcDate = '2024-05-21T10:00:00Z'; // 10:00 AM UTC
      const formatted = formatVietnamTime(utcDate);
      // Vietnam is UTC+7, so it should be 17:00
      // The exact format might vary depending on Node version, but typically it contains "17:00"
      expect(formatted).toMatch(/17:00/);
      expect(formatted).toMatch(/2024/);
    });
  });

  describe('capitalizeFirstWord', () => {
    it('should capitalize the first letter and lowercase the rest', () => {
      expect(capitalizeFirstWord('hELLO')).toBe('Hello');
      expect(capitalizeFirstWord('world')).toBe('World');
    });

    it('should return empty string for empty input', () => {
      expect(capitalizeFirstWord('')).toBe('');
    });
  });
});
