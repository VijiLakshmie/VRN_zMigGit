import { useEffect, useState } from 'react';
import './styles/mapping.css';

function Mapping() {
  const [mappingData, setMappingData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [rawResponse, setRawResponse] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchMapping = async () => {
      try {
        const response = await fetch('http://localhost:9090/mapping');

        if (!response.ok) {
          throw new Error(`Server responded with status ${response.status}`);
        }

        const text = await response.text();
        setRawResponse(text);

        try {
          const json = JSON.parse(text);
          if (json && typeof json === 'object') {
            setMappingData(json);
          } else {
            setError('⚠️ Invalid JSON structure.');
          }
        } catch (jsonError) {
          setError('⚠️ Response is not valid JSON. Check backend.');
        }
      } catch (err) {
        setError(`❌ Fetch error: ${err.message}`);
      } finally {
        setLoading(false);
      }
    };

    fetchMapping();
  }, []);

  return (
    <div className="mapping_section">
      <h2>SOURCE & TARGET EQUIVALENCE</h2>

      {loading ? (
        <>
          <div className="spinner"></div>
          <p className="loading_text"> Loading mapping...</p>
        </>

      ) : error ? (
        <p className="error_text">{error}</p>
      ) : mappingData ? (
        <>
          <table className="mapping_table">
            <thead>
              <tr>
                <th>ENDEVOR</th>
                <th>GIT BASED</th>
              </tr>
            </thead>
            <tbody>
              {Object.entries(mappingData).map(([key, value]) => (
                <tr key={key}>
                  <td>
                    {key.split('\n').map((line, idx) => (
                      <div key={idx}>{line}</div>
                    ))}
                  </td>

                  <td>
                    {value.split('\n').map((line, idx) => (
                      <div key={idx}>{line}</div>
                    ))}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </>
      ) : (
        <p className="empty_text">⚠️ No mapping data found.</p>
      )}
    </div>
  );
}

export default Mapping;
