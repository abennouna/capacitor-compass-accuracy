# Usage Examples

This directory contains examples demonstrating how to use the capacitor-compass-accuracy plugin.

## Basic Example

```typescript
import { CompassAccuracy, AccuracyLevel, ResultType } from 'capacitor-compass-accuracy';

// Start monitoring compass accuracy
async function startCompassMonitoring() {
  try {
    const callbackId = await CompassAccuracy.startMonitoring(
      { requiredAccuracy: AccuracyLevel.HIGH },
      (result, error) => {
        if (error) {
          console.error('Error monitoring compass:', error);
          return;
        }

        if (result) {
          console.log('Result type:', result.type);
          console.log('Current accuracy:', result.currentAccuracy);
          console.log('Required accuracy:', result.requiredAccuracy);
          
          if (result.type === ResultType.STARTED) {
            console.log('Compass monitoring started');
          } else if (result.type === ResultType.ACCURACY_CHANGED) {
            console.log('Accuracy changed from:', result.previousAccuracy);
          }
        }
      }
    );
    
    console.log('Monitoring callback ID:', callbackId);
  } catch (error) {
    console.error('Failed to start monitoring:', error);
  }
}

// Stop monitoring
async function stopCompassMonitoring() {
  try {
    await CompassAccuracy.stopMonitoring();
    console.log('Compass monitoring stopped');
  } catch (error) {
    console.error('Failed to stop monitoring:', error);
  }
}

// Get current accuracy without monitoring
async function getCompassAccuracy() {
  try {
    const result = await CompassAccuracy.getCurrentAccuracy();
    console.log('Current accuracy:', result.currentAccuracy);
  } catch (error) {
    console.error('Failed to get accuracy:', error);
  }
}

// For testing: simulate accuracy change
async function testAccuracyChange() {
  try {
    await CompassAccuracy.simulateAccuracyChange({
      accuracy: AccuracyLevel.LOW
    });
    console.log('Simulated accuracy change to LOW');
  } catch (error) {
    console.error('Failed to simulate accuracy change:', error);
  }
}
```

## React Example

```typescript
import React, { useEffect, useState } from 'react';
import { CompassAccuracy, AccuracyLevel, AccuracyChangeResult } from 'capacitor-compass-accuracy';

function CompassMonitor() {
  const [accuracy, setAccuracy] = useState<string>('unknown');
  const [isMonitoring, setIsMonitoring] = useState(false);

  const startMonitoring = async () => {
    try {
      await CompassAccuracy.startMonitoring(
        { requiredAccuracy: AccuracyLevel.HIGH },
        (result, error) => {
          if (error) {
            console.error('Error:', error);
            return;
          }
          if (result) {
            setAccuracy(result.currentAccuracy);
          }
        }
      );
      setIsMonitoring(true);
    } catch (error) {
      console.error('Failed to start monitoring:', error);
    }
  };

  const stopMonitoring = async () => {
    try {
      await CompassAccuracy.stopMonitoring();
      setIsMonitoring(false);
    } catch (error) {
      console.error('Failed to stop monitoring:', error);
    }
  };

  return (
    <div>
      <h2>Compass Accuracy Monitor</h2>
      <p>Current Accuracy: {accuracy}</p>
      <button onClick={isMonitoring ? stopMonitoring : startMonitoring}>
        {isMonitoring ? 'Stop Monitoring' : 'Start Monitoring'}
      </button>
    </div>
  );
}

export default CompassMonitor;
```

## Angular Example

```typescript
import { Component, OnInit, OnDestroy } from '@angular/core';
import { CompassAccuracy, AccuracyLevel, AccuracyChangeResult } from 'capacitor-compass-accuracy';

@Component({
  selector: 'app-compass-monitor',
  template: `
    <div>
      <h2>Compass Accuracy Monitor</h2>
      <p>Current Accuracy: {{ currentAccuracy }}</p>
      <button (click)="toggleMonitoring()">
        {{ isMonitoring ? 'Stop Monitoring' : 'Start Monitoring' }}
      </button>
    </div>
  `
})
export class CompassMonitorComponent implements OnInit, OnDestroy {
  currentAccuracy: string = 'unknown';
  isMonitoring: boolean = false;

  async toggleMonitoring() {
    if (this.isMonitoring) {
      await this.stopMonitoring();
    } else {
      await this.startMonitoring();
    }
  }

  async startMonitoring() {
    try {
      await CompassAccuracy.startMonitoring(
        { requiredAccuracy: AccuracyLevel.HIGH },
        (result, error) => {
          if (error) {
            console.error('Error:', error);
            return;
          }
          if (result) {
            this.currentAccuracy = result.currentAccuracy;
          }
        }
      );
      this.isMonitoring = true;
    } catch (error) {
      console.error('Failed to start monitoring:', error);
    }
  }

  async stopMonitoring() {
    try {
      await CompassAccuracy.stopMonitoring();
      this.isMonitoring = false;
    } catch (error) {
      console.error('Failed to stop monitoring:', error);
    }
  }

  ngOnDestroy() {
    if (this.isMonitoring) {
      this.stopMonitoring();
    }
  }
}
```

## Vue Example

```vue
<template>
  <div>
    <h2>Compass Accuracy Monitor</h2>
    <p>Current Accuracy: {{ currentAccuracy }}</p>
    <button @click="toggleMonitoring">
      {{ isMonitoring ? 'Stop Monitoring' : 'Start Monitoring' }}
    </button>
  </div>
</template>

<script>
import { ref, onUnmounted } from 'vue';
import { CompassAccuracy, AccuracyLevel } from 'capacitor-compass-accuracy';

export default {
  name: 'CompassMonitor',
  setup() {
    const currentAccuracy = ref('unknown');
    const isMonitoring = ref(false);

    const startMonitoring = async () => {
      try {
        await CompassAccuracy.startMonitoring(
          { requiredAccuracy: AccuracyLevel.HIGH },
          (result, error) => {
            if (error) {
              console.error('Error:', error);
              return;
            }
            if (result) {
              currentAccuracy.value = result.currentAccuracy;
            }
          }
        );
        isMonitoring.value = true;
      } catch (error) {
        console.error('Failed to start monitoring:', error);
      }
    };

    const stopMonitoring = async () => {
      try {
        await CompassAccuracy.stopMonitoring();
        isMonitoring.value = false;
      } catch (error) {
        console.error('Failed to stop monitoring:', error);
      }
    };

    const toggleMonitoring = () => {
      if (isMonitoring.value) {
        stopMonitoring();
      } else {
        startMonitoring();
      }
    };

    onUnmounted(() => {
      if (isMonitoring.value) {
        stopMonitoring();
      }
    });

    return {
      currentAccuracy,
      isMonitoring,
      toggleMonitoring
    };
  }
};
</script>
```
