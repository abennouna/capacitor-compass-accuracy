import { CompassAccuracy } from '@abennouna/capacitor-compass-accuracy';

window.testEcho = () => {
    const inputValue = document.getElementById("echoInput").value;
    CompassAccuracy.echo({ value: inputValue })
}
