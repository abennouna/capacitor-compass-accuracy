export interface CompassAccuracyPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
