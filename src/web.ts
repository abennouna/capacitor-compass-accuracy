import { WebPlugin } from '@capacitor/core';

import type { CompassAccuracyPlugin } from './definitions';

export class CompassAccuracyWeb extends WebPlugin implements CompassAccuracyPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
