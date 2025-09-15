/**
 * Utility for combining CSS class names
 */

export type ClassValue = string | number | boolean | undefined | null | ClassValue[];

export function cn(...inputs: ClassValue[]): string {
  const classes: string[] = [];
  
  for (const input of inputs) {
    if (input) {
      if (typeof input === 'string' || typeof input === 'number') {
        classes.push(String(input));
      } else if (typeof input === 'object' && input !== null) {
        if (Array.isArray(input)) {
          classes.push(cn(...input));
        } else {
          // Handle object form: { 'class-name': boolean }
          for (const [key, value] of Object.entries(input)) {
            if (value) {
              classes.push(key);
            }
          }
        }
      }
    }
  }
  
  return classes.join(' ');
}