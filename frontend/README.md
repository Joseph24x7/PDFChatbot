# Document Summary Frontend

A modern React + Vite frontend application for uploading and processing PDF documents with AI-powered summaries and Q&A capabilities.

## Features

- 📄 **PDF Upload**: Drag-and-drop or click to upload PDF files
- ❓ **AI Queries**: Optional query/question input for specific document questions
- 🤖 **AI Responses**: Get AI-generated summaries and answers from your documents
- 📋 **Copy Functionality**: Easily copy responses to clipboard
- 🎨 **Modern UI**: Beautiful gradient interface with smooth animations
- 📱 **Responsive Design**: Works seamlessly on desktop and mobile devices
- ⚡ **Fast Build**: Vite for rapid development and optimized production builds

## Tech Stack

- **React 18.2.0** - UI library
- **Vite 5.0.0** - Build tool and dev server
- **Axios** - HTTP client for API calls
- **CSS3** - Styling with gradients, animations, and flexbox

## Project Structure

```
frontend/
├── src/
│   ├── components/
│   │   ├── DocumentUpload.jsx     # Main upload component
│   │   └── DocumentUpload.css     # Component styles
│   ├── api/
│   │   └── documentApi.js         # API client and endpoints
│   ├── App.jsx                    # Root app component
│   ├── App.css                    # App styles
│   ├── index.css                  # Global styles
│   └── main.jsx                   # Entry point
├── index.html                     # HTML template
├── vite.config.js                 # Vite configuration
├── package.json                   # Dependencies and scripts
├── eslint.config.js              # ESLint configuration
└── README.md                      # This file
```

## Getting Started

### Prerequisites

- Node.js 14+ and npm 6+
- Running Spring Boot backend on port 8080

### Development

```bash
# Install dependencies
npm install

# Start dev server (default: http://localhost:5173)
npm run dev
```

### Build for Production

```bash
# Build optimized production bundle
npm run build

# Preview the production build
npm run preview
```

## API Integration

The frontend communicates with the backend `/documents/upload` endpoint:

### Upload Document
- **Endpoint**: `POST /documents/upload`
- **Parameters**:
  - `file` (FormData): PDF file
  - `query` (optional): Question or query about the document
- **Response**:
  ```json
  {
    "query": "User's question",
    "response": "AI-generated response"
  }
  ```

## Environment Variables

Create `.env` file for environment-specific configurations:

```env
VITE_API_URL=http://localhost:8080
```

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## Styling

The application uses CSS variables for easy theming:

```css
:root {
  --primary-color: #667eea;
  --primary-dark: #764ba2;
  --secondary-color: #f093fb;
  --success-color: #00d4ff;
  --error-color: #ff6b6b;
  /* ... more variables ... */
}
```

## Performance Optimizations

- Lazy loading of components
- Optimized bundle size with Vite
- CSS animations for smooth UX
- Efficient state management with React hooks

## Error Handling

The application handles various error scenarios:
- File validation (PDF format, file size)
- Network errors with user-friendly messages
- API error responses
- Form validation

## Features in Detail

### Drag & Drop Upload
- Drag PDF files directly onto the upload area
- Visual feedback during drag operations
- Automatic file validation

### Query Input
- Optional question or query field
- Textarea for longer prompts
- Sent along with the document for context-aware responses

### Response Display
- Query echo for context
- AI-generated response display
- Copy-to-clipboard functionality
- Success indication

## Development Scripts

```bash
npm run dev       # Start development server
npm run build     # Build for production
npm run preview   # Preview production build
npm run lint      # Run ESLint
```

## License

MIT License - feel free to use this project as you wish.

## Support

For issues or questions, please refer to the main project repository or contact the development team.

