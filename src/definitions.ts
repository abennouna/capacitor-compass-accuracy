export enum AccuracyLevel {
  /**
   * Indicates high accuracy (approximates to less than 5 degrees of error)
   */
  HIGH = 'high',
  /**
   * Indicates medium accuracy (approximates to less than 10 degrees of error)
   */
  MEDIUM = 'medium',
  /**
   * Indicates low accuracy (approximates to less than 15 degrees of error)
   */
  LOW = 'low',
  /**
   * Indicates unreliable accuracy (approximates to more than 15 degrees of error)
   */
  UNRELIABLE = 'unreliable',
  /**
   * Indicates an unknown accuracy value
   */
  UNKNOWN = 'unknown',
}

export enum ResultType {
  /**
   * Indicates that the monitor has been started and the current accuracy is being returned
   */
  STARTED = 'started',
  /**
   * Indicates that the accuracy has changed and the new accuracy is being returned
   */
  ACCURACY_CHANGED = 'accuracy_changed',
}

export interface AccuracyChangeResult {
  /**
   * The type of result being returned
   */
  type: ResultType;
  /**
   * The current accuracy of the device compass
   */
  currentAccuracy: AccuracyLevel;
  /**
   * The required accuracy of the device compass
   */
  requiredAccuracy: AccuracyLevel;
  /**
   * The previous accuracy of the device compass (only present when type is ACCURACY_CHANGED)
   */
  previousAccuracy?: AccuracyLevel;
}

export interface StartMonitoringOptions {
  /**
   * The required accuracy of the device compass. Defaults to HIGH if not specified.
   */
  requiredAccuracy?: AccuracyLevel;
}

export interface CompassAccuracyPlugin {
  /**
   * Starts monitoring the accuracy of the device compass for the required accuracy level.
   * 
   * If the initial or subsequent accuracy is less than the required accuracy, the plugin will
   * display a native dialog to the user requesting that they calibrate the compass.
   * The dialog will be displayed once per app session.
   *
   * @param options Configuration options for monitoring
   * @param callback Callback function that receives accuracy change results
   */
  startMonitoring(
    options: StartMonitoringOptions,
    callback: (result: AccuracyChangeResult | null, error?: any) => void
  ): Promise<string>;

  /**
   * Stops monitoring the accuracy of the device compass.
   */
  stopMonitoring(): Promise<void>;

  /**
   * Gets the current accuracy of the device compass.
   *
   * @returns A promise that resolves with the current accuracy
   */
  getCurrentAccuracy(): Promise<{ currentAccuracy: AccuracyLevel }>;

  /**
   * Simulates a change in the accuracy of the device compass to the specified accuracy.
   * This method is intended for use in testing only.
   *
   * @param options The simulated accuracy level
   */
  simulateAccuracyChange(options: {
    accuracy: AccuracyLevel;
  }): Promise<void>;
}
