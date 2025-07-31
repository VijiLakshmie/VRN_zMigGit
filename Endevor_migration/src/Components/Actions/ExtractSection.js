import { FaCloudUploadAlt } from "react-icons/fa";
import { LuFileJson } from "react-icons/lu";
import { PiArrowFatLineDownFill } from "react-icons/pi";

function ExtractSection({ selectedFile, handleFileChange }) {
  return (
    <div className="chosefile">
      <h3>Drop a JSON file here</h3>
      <p className="arrow"><PiArrowFatLineDownFill /></p>

      <input
        type="file"
        id="fileUpload"
        accept=".json"
        onChange={handleFileChange}
        className="upload_input"
      />

      <label htmlFor="fileUpload" className="upload_button">
        <FaCloudUploadAlt className="upload_icon" />
        Choose File
      </label>

      {selectedFile && (
        <p className="file_name">Uploaded JSON file <LuFileJson /> {selectedFile.name}</p>
      )}
    </div>
  );
}

export default ExtractSection;
