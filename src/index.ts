import { registerPlugin } from '@capacitor/core';

import type { CompassAccuracyPlugin } from './definitions';

const CompassAccuracy = registerPlugin<CompassAccuracyPlugin>('CompassAccuracy', {
  web: () => import('./web').then((m) => new m.CompassAccuracyWeb()),
});

export * from './definitions';
export { CompassAccuracy };
