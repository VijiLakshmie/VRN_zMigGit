import './styles/source.css'
function VideoSection() {
  return (
    <div className="video_section">
      <h3>zMigGIT Project</h3>
      <video width="100%" height="80%" controls>
        <source src="videos\Endevor Source Application 1507.mp4" type="video/mp4" />
        Your browser does not support the video tag.
      </video>
    </div>
  );
}

export default VideoSection;
