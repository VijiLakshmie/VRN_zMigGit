import React, { useEffect, useState } from "react";
import { MdAutoDelete } from "react-icons/md";
import './styles/Refresh.css';

function RefreshSection({
  setFileUploaded,
  setLoadedToDB,
  setTransformed,
  setSelectedFile,
  setPlatform
}) {
  const [structure, setStructure] = useState(null);
  const [status, setStatus] = useState("");
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    fetchStructure();
  }, []);

  const fetchStructure = async () => {
    try {
      const response = await fetch("http://localhost:9090/refresh-structure");
      const data = await response.json();

      if (Object.keys(data).length === 0) {
        setStatus("📁 Folder is already empty.");
      } else {
        setStructure(data);
        setStatus("");
      }
    } catch (error) {
      console.error("Error fetching structure:", error);
      setStatus("🚫 Failed to load folder structure.");
    }
  };

  const handleDelete = async () => {
    setDeleting(true);
    try {
      const response = await fetch("http://localhost:9090/refresh", {
        method: "GET",
      });
      if (response.ok) {
        const msg = await response.text();
        setStatus(`✅ ${msg}`);
        setStructure(null);

        // Reset container state
        setFileUploaded(false);
        setLoadedToDB(false);
        setTransformed(false);
        setSelectedFile(null);
        setPlatform('');
      } else {
        setStatus("❌ Failed to delete files.");
      }
    } catch (error) {
      console.error("Error during delete:", error);
      setStatus("🚫 Server error while deleting files.");
    } finally {
      setDeleting(false);
    }
  };

  const renderTree = (node, depth = 0, isLast = true) => {
    const entries = Object.entries(node);
    return entries.map(([key, value], index) => {
      const isFolder = typeof value === 'object';
      const prefix = depth === 0 ? '' :
        `${'│   '.repeat(depth - 1)}${isLast && index === entries.length - 1 ? '└── ' : '├── '}`;

      return (
        <div key={key} className="folder-tree-line">
          <div style={{ whiteSpace: "pre", fontFamily: "monospace" }}>
            {prefix}📁 {key}
          </div>
          {isFolder && renderTree(value, depth + 1, index === entries.length - 1)}
        </div>
      );
    });
  };

  return (
    <div className="refresh-section">
      {structure && (
        <div className="inside_folder">
          <div className="folder-view">
            <h3>Folder Structure (to be deleted):</h3>
            {renderTree(structure)}
          </div>
          <button className="del_btns" onClick={handleDelete} disabled={deleting}>
            <MdAutoDelete /> {deleting ? "Deleting..." : " Confirm Delete"}
          </button>
        </div>
      )}
      {status && <p>{status}</p>}
    </div>
  );
}

export default RefreshSection;
