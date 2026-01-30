import { WebPlugin } from '@capacitor/core';

import type {
  AccuracyChangeResult,
  AccuracyLevel,
  CompassAccuracyPlugin,
  StartMonitoringOptions,
} from './definitions';
import { ResultType } from './definitions';

export class CompassAccuracyWeb
  extends WebPlugin
  implements CompassAccuracyPlugin
{
  async startMonitoring(
    _options: StartMonitoringOptions,
    _callback: (result: AccuracyChangeResult | null, error?: any) => void,
  ): Promise<string> {
    throw this.unimplemented('Compass accuracy monitoring is not available on web');
  }

  async stopMonitoring(): Promise<void> {
    throw this.unimplemented('Compass accuracy monitoring is not available on web');
  }

  async getCurrentAccuracy(): Promise<{ currentAccuracy: AccuracyLevel }> {
    throw this.unimplemented('Compass accuracy is not available on web');
  }

  async simulateAccuracyChange(_options: {
    accuracy: AccuracyLevel;
  }): Promise<void> {
    throw this.unimplemented('Simulating accuracy changes is not available on web');
  }
}
