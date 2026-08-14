import 'Frontend/generated/jar-resources/flow-component-renderer.js';
import '@vaadin/polymer-legacy-adapter/style-modules.js';
import 'Frontend/generated/jar-resources/vaadin-grid-flow-selection-column.js';
import '@vaadin/app-layout/src/vaadin-app-layout.js';
import '@vaadin/tooltip/src/vaadin-tooltip.js';
import '@vaadin/icon/src/vaadin-icon.js';
import '@vaadin/context-menu/src/vaadin-context-menu.js';
import 'Frontend/generated/jar-resources/contextMenuConnector.js';
import 'Frontend/generated/jar-resources/contextMenuTargetConnector.js';
import '@vaadin/grid/src/vaadin-grid.js';
import '@vaadin/grid/src/vaadin-grid-column.js';
import '@vaadin/grid/src/vaadin-grid-sorter.js';
import '@vaadin/checkbox/src/vaadin-checkbox.js';
import 'Frontend/generated/jar-resources/gridConnector.ts';
import '@vaadin/button/src/vaadin-button.js';
import 'Frontend/generated/jar-resources/buttonFunctions.js';
import '@vaadin/text-field/src/vaadin-text-field.js';
import '@vaadin/icons/vaadin-iconset.js';
import '@vaadin/dialog/src/vaadin-dialog.js';
import '@vaadin/vertical-layout/src/vaadin-vertical-layout.js';
import '@vaadin/app-layout/src/vaadin-drawer-toggle.js';
import '@vaadin/horizontal-layout/src/vaadin-horizontal-layout.js';
import '@vaadin/avatar/src/vaadin-avatar.js';
import '@vaadin/grid/src/vaadin-grid-column-group.js';
import 'Frontend/generated/jar-resources/lit-renderer.ts';
import '@vaadin/notification/src/vaadin-notification.js';
import '@vaadin/login/src/vaadin-login-form.js';
import '@vaadin/common-frontend/ConnectionIndicator.js';
import '@vaadin/vaadin-lumo-styles/color-global.js';
import '@vaadin/vaadin-lumo-styles/typography-global.js';
import '@vaadin/vaadin-lumo-styles/sizing.js';
import '@vaadin/vaadin-lumo-styles/spacing.js';
import '@vaadin/vaadin-lumo-styles/style.js';
import '@vaadin/vaadin-lumo-styles/vaadin-iconset.js';

const loadOnDemand = (key) => {
  const pending = [];
  if (key === 'd707760d5c4cb866f730a78ebbe6dcea2e48506900a0d7393bde6d86889faff8') {
    pending.push(import('./chunks/chunk-987a1120f0c82e0c8c914efe528dfc397f406f7185270804fa5f1b6d828f0ffd.js'));
  }
  if (key === '62443ab6f3feb634a471089d175fad755d119a0496cb1ca30754fc1ec1c8c192') {
    pending.push(import('./chunks/chunk-23df50310e3f2dc7685c96c5005ea5272411e64e7abfb4c7ce4c89ab879878e9.js'));
  }
  if (key === 'b8fb286eecb732153f493a56a753b310572ce368e6f9b92e3a152254936438d6') {
    pending.push(import('./chunks/chunk-23df50310e3f2dc7685c96c5005ea5272411e64e7abfb4c7ce4c89ab879878e9.js'));
  }
  if (key === 'b42675bfec6975f51434878a7a073a9f80a34dc7a6f5c439abae5b8e6d757281') {
    pending.push(import('./chunks/chunk-2dc6ee6739326c8711b72616b8d381a250517c9f1513021425173512984ee07b.js'));
  }
  if (key === '32f6101d32f91e18cc91ee5a6be97128e26ffdc9c177aa1b151004891e5ca576') {
    pending.push(import('./chunks/chunk-23df50310e3f2dc7685c96c5005ea5272411e64e7abfb4c7ce4c89ab879878e9.js'));
  }
  return Promise.all(pending);
}

window.Vaadin = window.Vaadin || {};
window.Vaadin.Flow = window.Vaadin.Flow || {};
window.Vaadin.Flow.loadOnDemand = loadOnDemand;
window.Vaadin.Flow.resetFocus = () => {
 let ae=document.activeElement;
 while(ae&&ae.shadowRoot) ae = ae.shadowRoot.activeElement;
 return !ae || ae.blur() || ae.focus() || true;
}