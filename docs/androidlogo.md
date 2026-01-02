<svg width="512" height="512" viewBox="0 0 512 512" xmlns="http://www.w3.org/2000/svg">
  <defs>
    <filter id="glow" x="-50%" y="-50%" width="200%" height="200%">
      <feGaussianBlur stdDeviation="6" result="coloredBlur"/>
      <feMerge>
        <feMergeNode in="coloredBlur"/>
        <feMergeNode in="SourceGraphic"/>
      </feMerge>
    </filter>
    <clipPath id="ballClip">
      <circle cx="256" cy="256" r="180" />
    </clipPath>
  </defs>

  <rect x="0" y="0" width="512" height="512" rx="100" ry="100" fill="#4CAF50" />

  <g clip-path="url(#ballClip)">
    <circle cx="256" cy="256" r="180" fill="#FFFFFF" />

    <path d="M160 256 L120 190 L60 210 L60 300 L120 320 Z" fill="#000000" />
    <path d="M352 256 L392 190 L452 210 L452 300 L392 320 Z" fill="#000000" />
    <path d="M256 120 L210 60 L302 60 Z" fill="#000000" />
    <path d="M256 392 L210 452 L302 452 Z" fill="#000000" />
    <path d="M100 100 L150 120 L120 60 Z" fill="#000000" />
    <path d="M412 100 L362 120 L392 60 Z" fill="#000000" />
  </g>

<path d="M256 76
L256 196
C 256 196, 206 196, 206 256
C 206 316, 256 316, 256 316
L256 436"
stroke="#00FFFF"
stroke-width="12"
fill="none"
stroke-linecap="round"
filter="url(#glow)" />

  <ellipse cx="320" cy="180" rx="60" ry="30" fill="white" fill-opacity="0.3" transform="rotate(-45 320 180)" />

</svg>